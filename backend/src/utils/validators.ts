// src/utils/validators.ts
import { z } from 'zod';

export const emailSchema = z.string().email('Email inválido');
export const passwordSchema = z.string().min(8, 'La contraseña debe tener al menos 8 caracteres');
export const nameSchema = z.string().min(2, 'El nombre debe tener al menos 2 caracteres');

export const mealTypeSchema = z.enum(['breakfast', 'lunch', 'dinner', 'snack']);
export const dateSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Formato de fecha inválido (YYYY-MM-DD)');
