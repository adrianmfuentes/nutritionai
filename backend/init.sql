<<<<<<< HEAD
-- Crear la base de datos si no existe
-- Nota: En algunos entornos de despliegue gestionados, la BD se crea externamente.
-- CREATE DATABASE IF NOT EXISTS nutrition_app;

-- Habilitar extensión para UUIDs si es PostgreSQL (comentar si es MySQL/MariaDB y usar mecanismos nativos)
=======
-- Extensiones necesarias
>>>>>>> c37c5f7 (Fix: backend stability, ARM compatibility (bcryptjs) and schema sync)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
<<<<<<< HEAD
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    height FLOAT, -- en cm
    weight FLOAT, -- en kg
=======
    name VARCHAR(100),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    height DOUBLE PRECISION,
    weight DOUBLE PRECISION,
>>>>>>> c37c5f7 (Fix: backend stability, ARM compatibility (bcryptjs) and schema sync)
    age INTEGER,
    gender VARCHAR(20),
    activity_level VARCHAR(50),
    caloric_goal INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

<<<<<<< HEAD
-- Tabla de Comidas (Meals)
CREATE TABLE IF NOT EXISTS meals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255), -- Ej: "Desayuno", "Almuerzo"
    image_url TEXT,
    date_consumed TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    analysis_status VARCHAR(50) DEFAULT 'pending', -- pending, completed, failed
    total_calories INTEGER DEFAULT 0,
    total_protein FLOAT DEFAULT 0,
    total_carbs FLOAT DEFAULT 0,
    total_fat FLOAT DEFAULT 0,
    raw_ai_analysis JSONB, -- Guardar la respuesta cruda de la IA por si acaso
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Alimentos (Foods) - Items individuales detectados dentro de una comida
CREATE TABLE IF NOT EXISTS foods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    meal_id UUID NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    quantity FLOAT,
    unit VARCHAR(50),
    calories INTEGER,
    protein FLOAT,
    carbs FLOAT,
    fat FLOAT,
    confidence_score FLOAT, -- Nivel de confianza de la IA
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Chat/Historial de mensajes con la IA
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL, -- 'user' o 'assistant'
    content TEXT NOT NULL,
    context_meal_id UUID REFERENCES meals(id) ON DELETE SET NULL, -- Si el mensaje es sobre una comida específica
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_meals_user_id ON meals(user_id);
CREATE INDEX IF NOT EXISTS idx_meals_date ON meals(date_consumed);
CREATE INDEX IF NOT EXISTS idx_foods_meal_id ON foods(meal_id);
CREATE INDEX IF NOT EXISTS idx_chat_user_id ON chat_messages(user_id);
=======
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
>>>>>>> c37c5f7 (Fix: backend stability, ARM compatibility (bcryptjs) and schema sync)
