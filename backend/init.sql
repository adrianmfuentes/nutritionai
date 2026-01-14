-- Crear la base de datos si no existe
-- Nota: En algunos entornos de despliegue gestionados, la BD se crea externamente.
-- CREATE DATABASE IF NOT EXISTS nutrition_app;

-- Habilitar extensión para UUIDs si es PostgreSQL (comentar si es MySQL/MariaDB y usar mecanismos nativos)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    height FLOAT, -- en cm
    weight FLOAT, -- en kg
    age INTEGER,
    gender VARCHAR(20),
    activity_level VARCHAR(50),
    caloric_goal INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

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