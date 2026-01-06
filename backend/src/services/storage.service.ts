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
      // Crear directorio para el usuario
      const userDir = path.join(this.uploadPath, userId);
      await fs.mkdir(userDir, { recursive: true });

      // Generar nombre único
      const uniqueId = crypto.randomBytes(16).toString('hex');
      const filename = `meal_${Date.now()}_${uniqueId}.jpg`;
      const targetPath = path.join(userDir, filename);

      // Optimizar y guardar imagen
      await sharp(tempFilePath)
        .resize(1200, 1200, {
          fit: 'inside',
          withoutEnlargement: true,
        })
        .jpeg({ quality: 85 })
        .toFile(targetPath);

      // Eliminar archivo temporal
      await fs.unlink(tempFilePath);

      // Retornar URL relativa
      return `/uploads/${userId}/${filename}`;
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
