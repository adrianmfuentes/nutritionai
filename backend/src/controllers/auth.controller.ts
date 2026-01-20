// src/controllers/auth.controller.ts
import { Request, Response, NextFunction } from 'express';
import bcrypt from 'bcryptjs';
import { pool } from '../config/database';
import { generateToken } from '../utils/jwt';
import { z } from 'zod';
import { logger } from '../utils/logger';
import { StorageService } from '../services/storage.service';

const RegisterSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
  name: z.string().min(2, 'El nombre debe tener al menos 2 caracteres'),
});

const LoginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(1, 'La contraseña es requerida'),
});

const ChangePasswordSchema = z.object({
  currentPassword: z.string().min(1, 'La contraseña actual es requerida'),
  newPassword: z.string().min(8, 'La nueva contraseña debe tener al menos 8 caracteres'),
});

const UpdateProfileSchema = z.object({
  name: z.string().min(2, 'El nombre debe tener al menos 2 caracteres').optional(),
  profile_photo: z.string().nullable().optional(),
});

export class AuthController {
  private storageService = new StorageService();
  async register(req: Request, res: Response, next: NextFunction) {
    try {
      const validatedData = RegisterSchema.parse(req.body);
      const { email, password, name } = validatedData;

      // Verificar si el usuario ya existe
      const existingUser = await pool.query(
        'SELECT id FROM users WHERE email = $1',
        [email]
      );

      if (existingUser.rows.length > 0) {
        return res.status(409).json({ error: 'El email ya está registrado' });
      }

      // Hash de la contraseña
      const passwordHash = await bcrypt.hash(password, 12);

      // Crear usuario
      const result = await pool.query(
        `INSERT INTO users (email, password_hash, name) 
         VALUES ($1, $2, $3) 
         RETURNING id, email, name, created_at`,
        [email, passwordHash, name]
      );

      const user = result.rows[0];

      // Crear goals por defecto (2000 cal, 150g proteína, 200g carbos, 65g grasa)
      await pool.query(
        `INSERT INTO nutrition_goals (user_id, daily_calories, daily_protein, daily_carbs, daily_fat, active_from)
         VALUES ($1, $2, $3, $4, $5, CURRENT_DATE)`,
        [user.id, 2000, 150, 200, 65]
      );

      // Generar token
      const token = generateToken(user.id);

      logger.info(`Usuario registrado: ${email}`);

      res.status(201).json({
        message: 'Usuario registrado exitosamente',
        user: {
          id: user.id,
          email: user.email,
          name: user.name,
        },
        token,
      });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          details: error.issues 
        });
      }
      next(error);
    }
  }

  async login(req: Request, res: Response, next: NextFunction) {
    try {
      const validatedData = LoginSchema.parse(req.body);
      const { email, password } = validatedData;

      // Buscar usuario
      const result = await pool.query(
        'SELECT id, email, name, password_hash FROM users WHERE email = $1',
        [email]
      );

      if (result.rows.length === 0) {
        logger.warn(`Intento de login fallido: Usuario no encontrado (${email})`);
        return res.status(401).json({ error: 'Credenciales inválidas' });
      }

      const user = result.rows[0];

      // Verificar contraseña
      const isValidPassword = await bcrypt.compare(password, user.password_hash);

      if (!isValidPassword) {
        logger.warn(`Intento de login fallido: Contraseña incorrecta para ${email}`);
        return res.status(401).json({ error: 'Credenciales inválidas' });
      }

      // Generar token
      const token = generateToken(user.id);

      logger.info(`Usuario autenticado: ${email}`);

      res.json({
        message: 'Inicio de sesión exitoso',
        user: {
          id: user.id,
          email: user.email,
          name: user.name,
        },
        token,
      });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          details: error.issues 
        });
      }
      next(error);
    }
  }

  async changePassword(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const validatedData = ChangePasswordSchema.parse(req.body);
      const { currentPassword, newPassword } = validatedData;
      
      const result = await pool.query(
        'SELECT password_hash FROM users WHERE id = $1',
        [userId]
      );

      if (result.rows.length === 0) {
        return res.status(404).json({ error: 'Usuario no encontrado' });
      }

      const user = result.rows[0];

      const isValidPassword = await bcrypt.compare(currentPassword, user.password_hash);
      if (!isValidPassword) {
        return res.status(401).json({ error: 'La contraseña actual es incorrecta' });
      }

      const newPasswordHash = await bcrypt.hash(newPassword, 12);

      await pool.query(
        'UPDATE users SET password_hash = $1 WHERE id = $2',
        [newPasswordHash, userId]
      );

      logger.info(`Contraseña actualizada para usuario ID: ${userId}`);

      res.json({ message: 'Contraseña actualizada exitosamente' });
    } catch (error) {
      if (error instanceof z.ZodError) {
        return res.status(400).json({ 
          error: 'Validación fallida', 
          details: error.issues 
        });
      }
      next(error);
    }
  }

  async getProfile(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;

      // Obtener usuario y sus objetivos más recientes
      const result = await pool.query(
        `SELECT u.id, u.email, u.name, u.profile_photo, u.created_at,
                ng.daily_calories, ng.daily_protein, ng.daily_carbs, ng.daily_fat
         FROM users u
         LEFT JOIN nutrition_goals ng ON u.id = ng.user_id
         WHERE u.id = $1
         ORDER BY ng.active_from DESC
         LIMIT 1`,
        [userId]
      );

      if (result.rows.length === 0) {
        return res.status(404).json({ error: 'Usuario no encontrado' });
      }

      const row = result.rows[0];
      
      const user = {
        id: row.id,
        email: row.email,
        name: row.name,
        profile_photo: row.profile_photo,
        created_at: row.created_at
      };

      const goals = row.daily_calories ? {
        calories: row.daily_calories,
        protein: row.daily_protein,
        carbs: row.daily_carbs,
        fat: row.daily_fat
      } : null;

      // Estructura que espera el frontend (ProfileResponse)
      // { user: UserProfileDto, goals: NutritionGoalsDto }
      res.json({ user, goals });
    } catch (error) {
      next(error);
    }
  }

  async updateProfile(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;
      const validatedData = UpdateProfileSchema.parse(req.body);

      let imageUrl: string | null | undefined;

      // Si se proporciona una imagen, guardarla
      if (req.files && Array.isArray(req.files) && req.files.length > 0) {
        const file = req.files[0] as Express.Multer.File;

        if (file && file.path) {
          imageUrl = await this.storageService.saveProfileImage(file.path, userId);

          // Obtener la foto anterior para eliminarla
          const currentUser = await pool.query(
            'SELECT profile_photo FROM users WHERE id = $1',
            [userId]
          );

          if (currentUser.rows[0]?.profile_photo) {
            await this.storageService.deleteImage(currentUser.rows[0].profile_photo);
          }
        }
      } else if (validatedData.profile_photo === null) {
        // Eliminar foto si se solicita
        const currentUser = await pool.query(
          'SELECT profile_photo FROM users WHERE id = $1',
          [userId]
        );

        if (currentUser.rows[0]?.profile_photo) {
          await this.storageService.deleteImage(currentUser.rows[0].profile_photo);
        }
        imageUrl = null;
      }

      // Actualizar usuario
      const updateFields = [];
      const updateValues = [];
      let paramIndex = 1;

      if (validatedData.name !== undefined) {
        updateFields.push(`name = $${paramIndex++}`);
        updateValues.push(validatedData.name);
      }

      if (imageUrl !== undefined) {
        updateFields.push(`profile_photo = $${paramIndex++}`);
        updateValues.push(imageUrl);
      }

      updateFields.push(`updated_at = CURRENT_TIMESTAMP`);

      updateValues.push(userId); // Para WHERE id = $paramIndex

      const updatedUser = await pool.query(
        `UPDATE users 
         SET ${updateFields.join(', ')} 
         WHERE id = $${paramIndex} 
         RETURNING id, email, name, profile_photo, created_at, updated_at`,
        updateValues
      );

      if (updatedUser.rows.length === 0) {
        return res.status(404).json({ error: 'Usuario no encontrado' });
      }

      res.json({ user: updatedUser.rows[0] });
    } catch (error) {
      next(error);
    }
  }

  async updateProfilePhoto(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;

      if (!req.file) {
        return res.status(400).json({ error: 'No se proporcionó ninguna imagen' });
      }

      // Guardar la nueva imagen
      const imageUrl = await this.storageService.saveProfileImage(req.file.path, userId);

      // Obtener la foto anterior para eliminarla
      const currentUser = await pool.query(
        'SELECT profile_photo FROM users WHERE id = $1',
        [userId]
      );

      if (currentUser.rows[0]?.profile_photo) {
        await this.storageService.deleteImage(currentUser.rows[0].profile_photo);
      }

      // Actualizar la base de datos
      const updatedUser = await pool.query(
        `UPDATE users 
         SET profile_photo = $1, updated_at = CURRENT_TIMESTAMP 
         WHERE id = $2 
         RETURNING id, email, name, profile_photo, created_at, updated_at`,
        [imageUrl, userId]
      );

      if (updatedUser.rows.length === 0) {
        return res.status(404).json({ error: 'Usuario no encontrado' });
      }

      res.json({ user: updatedUser.rows[0] });
    } catch (error) {
      next(error);
    }
  }

  async deleteProfilePhoto(req: Request, res: Response, next: NextFunction) {
    try {
      const userId = (req as any).user?.id;

      // Obtener la foto actual
      const currentUser = await pool.query(
        'SELECT profile_photo FROM users WHERE id = $1',
        [userId]
      );

      if (!currentUser.rows[0]?.profile_photo) {
        return res.status(404).json({ error: 'No hay foto de perfil para eliminar' });
      }

      // Eliminar la imagen del almacenamiento
      await this.storageService.deleteImage(currentUser.rows[0].profile_photo);

      // Actualizar la base de datos
      const updatedUser = await pool.query(
        `UPDATE users 
         SET profile_photo = NULL, updated_at = CURRENT_TIMESTAMP 
         WHERE id = $2 
         RETURNING id, email, name, profile_photo, created_at, updated_at`,
        [userId]
      );

      res.json({ user: updatedUser.rows[0] });
    } catch (error) {
      next(error);
    }
  }
}
