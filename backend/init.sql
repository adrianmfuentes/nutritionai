-- init.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Nutrition goals
CREATE TABLE nutrition_goals (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  daily_calories INTEGER NOT NULL,
  daily_protein INTEGER NOT NULL,
  daily_carbs INTEGER NOT NULL,
  daily_fat INTEGER NOT NULL,
  active_from DATE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, active_from)
);

CREATE INDEX idx_goals_user_date ON nutrition_goals(user_id, active_from DESC);

-- Meals
CREATE TABLE meals (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  meal_type VARCHAR(20) CHECK (meal_type IN ('breakfast', 'lunch', 'dinner', 'snack')),
  image_url TEXT,
  notes TEXT,
  total_calories INTEGER NOT NULL,
  total_protein DECIMAL(6,2) NOT NULL,
  total_carbs DECIMAL(6,2) NOT NULL,
  total_fat DECIMAL(6,2) NOT NULL,
  total_fiber DECIMAL(6,2),
  health_score DECIMAL(3,1),
  meal_date DATE NOT NULL,
  consumed_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meals_user_date ON meals(user_id, meal_date DESC);
CREATE INDEX idx_meals_consumed ON meals(consumed_at DESC);

-- Detected foods
CREATE TABLE detected_foods (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  meal_id UUID NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
  name VARCHAR(200) NOT NULL,
  confidence DECIMAL(3,2) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  portion_amount DECIMAL(8,2) NOT NULL,
  portion_unit VARCHAR(20) NOT NULL,
  calories INTEGER NOT NULL,
  protein DECIMAL(6,2) NOT NULL,
  carbs DECIMAL(6,2) NOT NULL,
  fat DECIMAL(6,2) NOT NULL,
  fiber DECIMAL(6,2),
  category VARCHAR(20) CHECK (category IN ('protein', 'carb', 'vegetable', 'fruit', 'dairy', 'fat', 'mixed')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_foods_meal ON detected_foods(meal_id);

-- Views
CREATE VIEW daily_nutrition_summary AS
SELECT 
  user_id,
  meal_date,
  SUM(total_calories) as daily_calories,
  SUM(total_protein) as daily_protein,
  SUM(total_carbs) as daily_carbs,
  SUM(total_fat) as daily_fat,
  SUM(total_fiber) as daily_fiber,
  COUNT(*) as meal_count,
  AVG(health_score) as avg_health_score
FROM meals
GROUP BY user_id, meal_date;
