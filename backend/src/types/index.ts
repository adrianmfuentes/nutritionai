// src/types/index.ts

export interface User {
  id: string;
  email: string;
  name: string;
  profile_photo?: string;
  password_hash?: string;
  created_at: Date;
  updated_at: Date;
}

export interface NutritionGoal {
  id: string;
  user_id: string;
  daily_calories: number;
  daily_protein: number;
  daily_carbs: number;
  daily_fat: number;
  active_from: Date;
  created_at: Date;
}

export interface Meal {
  id: string;
  user_id: string;
  meal_type: 'breakfast' | 'lunch' | 'dinner' | 'snack';
  image_url: string;
  notes?: string;
  total_calories: number;
  total_protein: number;
  total_carbs: number;
  total_fat: number;
  total_fiber?: number;
  health_score?: number;
  meal_date: string;
  consumed_at: Date;
  created_at: Date;
  updated_at: Date;
  foods?: DetectedFood[];
}

export interface DetectedFood {
  id: string;
  meal_id: string;
  name: string;
  confidence: number;
  portion_amount: number;
  portion_unit: string;
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  fiber?: number;
  category: 'protein' | 'carb' | 'vegetable' | 'fruit' | 'dairy' | 'fat' | 'mixed';
  created_at: Date;
}

export interface NutritionData {
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  fiber?: number;
}

export interface Portion {
  amount: number;
  unit: string;
}

export interface VisionAnalysisFood {
  name: string;
  confidence: number;
  portion: Portion;
  nutrition: NutritionData;
  category: string;
  detected_ingredients?: string[];
  portion_display?: string;
  portion_grams?: number;
}

export interface VisionAnalysisResult {
  is_food: boolean;
  error: string | null;
  reasoning: string;
  foods: VisionAnalysisFood[];
  meal_analysis: {
    health_score: number;
    health_feedback: string;
    dominant_macro: string;
  };
  mealContext?: {
    estimatedMealType: string;
    portionSize: string;
    healthScore: number;
  };
  notes?: string;
  totalNutrition: NutritionData;
}
