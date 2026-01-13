// src/utils/llmSanitizers.ts
import { VisionAnalysisResult, VisionAnalysisFood } from '../types';
import { isClearlyNonFoodName } from './mealHeuristics';

const VALID_CATEGORIES = new Set(['protein', 'carb', 'vegetable', 'fruit', 'dairy', 'fat', 'mixed']);

function toStringOrEmpty(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}

function toFiniteNumber(value: unknown, fallback: number): number {
  if (typeof value === 'number') return Number.isFinite(value) ? value : fallback;
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (!trimmed) return fallback;
    const parsed = Number(trimmed);
    return Number.isFinite(parsed) ? parsed : fallback;
  }
  return fallback;
}

function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, value));
}

function sanitizeCategory(value: unknown): VisionAnalysisFood['category'] {
  const raw = toStringOrEmpty(value).toLowerCase();
  return (VALID_CATEGORIES.has(raw) ? raw : 'mixed') as VisionAnalysisFood['category'];
}

function sanitizeUnit(value: unknown): string {
  const unit = toStringOrEmpty(value).toLowerCase();
  if (!unit) return 'g';
  // Unidades típicas, pero no bloqueamos otras (evita rechazos innecesarios)
  if (unit.length > 16) return unit.slice(0, 16);
  return unit;
}

export function sanitizeVisionAnalysisResult(raw: any): VisionAnalysisResult {
  const foodsRaw = Array.isArray(raw?.foods) ? raw.foods : [];

  const foods: VisionAnalysisFood[] = foodsRaw
    .map((f: any) => {
      const name = toStringOrEmpty(f?.name);
      if (!name || isClearlyNonFoodName(name)) return null;

      const confidence = clamp(toFiniteNumber(f?.confidence, 0.5), 0, 1);
      const portionAmount = clamp(toFiniteNumber(f?.portion?.amount ?? f?.portion_amount, 0), 0, 9999.99);
      const portionUnit = sanitizeUnit(f?.portion?.unit ?? f?.portion_unit);

      const calories = clamp(toFiniteNumber(f?.nutrition?.calories ?? f?.calories, 0), 0, 99999);
      const protein = clamp(toFiniteNumber(f?.nutrition?.protein ?? f?.protein, 0), 0, 99999);
      const carbs = clamp(toFiniteNumber(f?.nutrition?.carbs ?? f?.carbs, 0), 0, 99999);
      const fat = clamp(toFiniteNumber(f?.nutrition?.fat ?? f?.fat, 0), 0, 99999);
      const fiber = toFiniteNumber(f?.nutrition?.fiber ?? f?.fiber, NaN);

      // Si todo está a cero y no hay porción, suele ser basura del modelo
      const hasSignal = portionAmount > 0 || calories > 0 || protein > 0 || carbs > 0 || fat > 0;
      if (!hasSignal) return null;

      return {
        name,
        confidence,
        portion: { amount: portionAmount, unit: portionUnit },
        nutrition: {
          calories,
          protein,
          carbs,
          fat,
          fiber: Number.isFinite(fiber) ? clamp(fiber, 0, 99999) : undefined,
        },
        category: sanitizeCategory(f?.category),
      } satisfies VisionAnalysisFood;
    })
    .filter(Boolean) as VisionAnalysisFood[];

  const totalRaw = raw?.totalNutrition ?? {};
  const totalsFromModel = {
    calories: clamp(toFiniteNumber(totalRaw?.calories, NaN), 0, 999999),
    protein: clamp(toFiniteNumber(totalRaw?.protein, NaN), 0, 999999),
    carbs: clamp(toFiniteNumber(totalRaw?.carbs, NaN), 0, 999999),
    fat: clamp(toFiniteNumber(totalRaw?.fat, NaN), 0, 999999),
    fiber: toFiniteNumber(totalRaw?.fiber, NaN),
  };

  const totalsComputed = foods.reduce(
    (acc, f) => {
      acc.calories += toFiniteNumber(f.nutrition.calories, 0);
      acc.protein += toFiniteNumber(f.nutrition.protein, 0);
      acc.carbs += toFiniteNumber(f.nutrition.carbs, 0);
      acc.fat += toFiniteNumber(f.nutrition.fat, 0);
      if (typeof f.nutrition.fiber === 'number') acc.fiber += toFiniteNumber(f.nutrition.fiber, 0);
      return acc;
    },
    { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 }
  );

  const useModelTotals =
    Number.isFinite(totalsFromModel.calories) &&
    Number.isFinite(totalsFromModel.protein) &&
    Number.isFinite(totalsFromModel.carbs) &&
    Number.isFinite(totalsFromModel.fat);

  const totalNutrition = useModelTotals
    ? {
        calories: totalsFromModel.calories,
        protein: totalsFromModel.protein,
        carbs: totalsFromModel.carbs,
        fat: totalsFromModel.fat,
        fiber: Number.isFinite(totalsFromModel.fiber) ? clamp(totalsFromModel.fiber, 0, 999999) : undefined,
      }
    : {
        calories: totalsComputed.calories,
        protein: totalsComputed.protein,
        carbs: totalsComputed.carbs,
        fat: totalsComputed.fat,
        fiber: totalsComputed.fiber > 0 ? totalsComputed.fiber : undefined,
      };

  const mc = raw?.mealContext ?? {};
  const healthScore = clamp(toFiniteNumber(mc?.healthScore, 5), 1, 10);

  return {
    foods,
    totalNutrition,
    mealContext: {
      estimatedMealType: toStringOrEmpty(mc?.estimatedMealType) || 'snack',
      portionSize: toStringOrEmpty(mc?.portionSize) || 'medium',
      healthScore,
    },
    notes: toStringOrEmpty(raw?.notes),
  };
}

export function sanitizeChatMealData(raw: any):
  | {
      foods: Array<{
        name: string;
        amount: number;
        unit: string;
        nutrition: { calories: number; protein: number; carbs: number; fat: number; fiber?: number };
        category: VisionAnalysisFood['category'];
      }>;
      totalNutrition: { calories: number; protein: number; carbs: number; fat: number; fiber?: number };
      mealType: string;
    }
  | undefined {
  const foodsRaw = Array.isArray(raw?.foods) ? raw.foods : [];

  const foods = foodsRaw
    .map((f: any) => {
      const name = toStringOrEmpty(f?.name);
      if (!name || isClearlyNonFoodName(name)) return null;

      const amount = clamp(toFiniteNumber(f?.amount ?? f?.portion?.amount, 0), 0, 9999.99);
      const unit = sanitizeUnit(f?.unit ?? f?.portion?.unit);

      const calories = clamp(toFiniteNumber(f?.nutrition?.calories, 0), 0, 99999);
      const protein = clamp(toFiniteNumber(f?.nutrition?.protein, 0), 0, 99999);
      const carbs = clamp(toFiniteNumber(f?.nutrition?.carbs, 0), 0, 99999);
      const fat = clamp(toFiniteNumber(f?.nutrition?.fat, 0), 0, 99999);
      const fiber = toFiniteNumber(f?.nutrition?.fiber, NaN);

      const hasSignal = amount > 0 || calories > 0 || protein > 0 || carbs > 0 || fat > 0;
      if (!hasSignal) return null;

      return {
        name,
        amount,
        unit,
        nutrition: {
          calories,
          protein,
          carbs,
          fat,
          fiber: Number.isFinite(fiber) ? clamp(fiber, 0, 99999) : undefined,
        },
        category: sanitizeCategory(f?.category),
      };
    })
    .filter(Boolean) as any[];

  if (foods.length === 0) return undefined;

  const tn = raw?.totalNutrition ?? {};
  const totalNutrition = {
    calories: clamp(toFiniteNumber(tn?.calories, foods.reduce((s, f) => s + f.nutrition.calories, 0)), 0, 999999),
    protein: clamp(toFiniteNumber(tn?.protein, foods.reduce((s, f) => s + f.nutrition.protein, 0)), 0, 999999),
    carbs: clamp(toFiniteNumber(tn?.carbs, foods.reduce((s, f) => s + f.nutrition.carbs, 0)), 0, 999999),
    fat: clamp(toFiniteNumber(tn?.fat, foods.reduce((s, f) => s + f.nutrition.fat, 0)), 0, 999999),
    fiber: (() => {
      const computed = foods.reduce((s, f) => s + (f.nutrition.fiber ?? 0), 0);
      const val = toFiniteNumber(tn?.fiber, computed);
      return Number.isFinite(val) && val > 0 ? clamp(val, 0, 999999) : undefined;
    })(),
  };

  return {
    foods,
    totalNutrition,
    mealType: toStringOrEmpty(raw?.mealType) || 'snack',
  };
}
