// src/middleware/auth.ts
import { Request, Response, NextFunction } from 'express';
import { verifyToken } from '../utils/jwt';
import { pool } from '../config/database';

export interface AuthRequest extends Request {
  user?: {
    id: string;
    email: string;
  };
}

export async function authenticate(
  req: AuthRequest,
  res: Response,
  next: NextFunction
) {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ error: 'Token de autenticación requerido' });
    }

    const token = authHeader.substring(7);

    try {
      const decoded = verifyToken(token);
      
      // Verificar que el usuario existe
      const result = await pool.query(
        'SELECT id, email FROM users WHERE id = $1',
        [decoded.userId]
      );

      if (result.rows.length === 0) {
        return res.status(401).json({ error: 'Usuario no encontrado' });
      }

      req.user = {
        id: result.rows[0].id,
        email: result.rows[0].email,
      };

      next();
    } catch (error) {
      return res.status(401).json({ error: 'Token inválido o expirado' });
    }
  } catch (error) {
    next(error);
  }
}
