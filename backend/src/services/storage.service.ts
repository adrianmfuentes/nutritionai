// src/services/storage.service.ts
import fs from 'fs/promises';
import path from 'path';
import sharp from 'sharp';
import crypto from 'crypto';
import { logger } from '../utils/logger';

export class StorageService {
  private uploadPath: string;

  constructor() {
    this.uploadPath = process.env.UPLOAD_PATH || './uploads';
  }

  async saveImage(tempFilePath: string, userId: string): Promise<string> {
    try {
      // Sanitizar userId para evitar path traversal
      const safeUserId = String(userId).replace(/[^a-zA-Z0-9_-]/g, '');
      // Crear directorio para el usuario
      const userDir = path.join(this.uploadPath, safeUserId);
      await fs.mkdir(userDir, { recursive: true });

      // Generar nombre único seguro
      const uniqueId = crypto.randomBytes(16).toString('hex');
      const filename = `meal_${Date.now()}_${uniqueId}.jpg`;
      // Asegurar que filename no contenga path separators
      const safeFilename = path.basename(filename);
      const targetPath = path.join(userDir, safeFilename);

      // Optimizar y guardar imagen
      await sharp(tempFilePath)
        .resize(1200, 1200, {
          fit: 'inside',
          withoutEnlargement: true,
        })
        .jpeg({ quality: 85 })
        .toFile(targetPath);

      // Eliminar archivo temporal de forma segura
      const tempDir = path.resolve('./uploads/temp');
      const absoluteTempFilePath = path.resolve(tempFilePath);
      if (absoluteTempFilePath.startsWith(tempDir + path.sep)) {
        await fs.unlink(absoluteTempFilePath).catch(() => undefined);
      }

      // Retornar URL relativa
      return `/uploads/${safeUserId}/${safeFilename}`;
    } catch (error) {
      logger.error('Error guardando imagen:', error);
      throw new Error('Error al guardar la imagen');
    }
  }

  async deleteImage(imageUrl: string): Promise<void> {
    try {
      // Convertir URL a path local
      const imagePath = path.join(this.uploadPath, imageUrl.replace('/uploads/', ''));
      
      // Verificar que existe y eliminar
      await fs.access(imagePath);
      await fs.unlink(imagePath);
      
      logger.info(`Imagen eliminada: ${imageUrl}`);
    } catch (error) {
      logger.error('Error eliminando imagen:', error);
      // No lanzar error, solo loguear
    }
  }

  async getImagePath(imageUrl: string): Promise<string> {
    return path.join(this.uploadPath, imageUrl.replace('/uploads/', ''));
  }
}
