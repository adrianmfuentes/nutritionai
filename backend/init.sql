-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    height DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    age INTEGER,
    gender VARCHAR(20),
    activity_level VARCHAR(50),
    caloric_goal INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Metas Nutricionales
CREATE TABLE IF NOT EXISTS nutrition_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    daily_calories DOUBLE PRECISION DEFAULT 2000,
    daily_protein DOUBLE PRECISION DEFAULT 150,
    daily_carbs DOUBLE PRECISION DEFAULT 250,
    daily_fat DOUBLE PRECISION DEFAULT 70,
    daily_fiber DOUBLE PRECISION DEFAULT 25,
    daily_sugar DOUBLE PRECISION DEFAULT 50,
    daily_sodium DOUBLE PRECISION DEFAULT 2300,
    active_from DATE DEFAULT CURRENT_DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);

-- Tabla de Comidas
CREATE TABLE IF NOT EXISTS meals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    meal_name VARCHAR(255),
    meal_type VARCHAR(50),
    total_calories DOUBLE PRECISION DEFAULT 0,
    total_protein DOUBLE PRECISION DEFAULT 0,
    total_carbs DOUBLE PRECISION DEFAULT 0,
    total_fat DOUBLE PRECISION DEFAULT 0,
    total_fiber DOUBLE PRECISION DEFAULT 0,
    total_sugar DOUBLE PRECISION DEFAULT 0,
    total_sodium DOUBLE PRECISION DEFAULT 0,
    health_score DOUBLE PRECISION DEFAULT 0,
    ai_analysis TEXT,
    notes TEXT,
    image_url VARCHAR(255),
    meal_date DATE DEFAULT CURRENT_DATE,
    consumed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Alimentos Detectados
CREATE TABLE IF NOT EXISTS detected_foods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meal_id UUID NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    portion_amount DOUBLE PRECISION DEFAULT 1,
    portion_unit VARCHAR(50) DEFAULT 'unidad',
    calories DOUBLE PRECISION DEFAULT 0,
    protein DOUBLE PRECISION DEFAULT 0,
    carbs DOUBLE PRECISION DEFAULT 0,
    fat DOUBLE PRECISION DEFAULT 0,
    fiber DOUBLE PRECISION DEFAULT 0,
    sugar DOUBLE PRECISION DEFAULT 0,
    sodium DOUBLE PRECISION DEFAULT 0,
    category VARCHAR(100),
    confidence DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
