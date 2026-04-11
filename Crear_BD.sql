-- ============================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS
-- GESTOR DE INCIDENCIAS
-- ============================================

-- CREACIÓN DE LA BASE DE DATOS
CREATE DATABASE IF NOT EXISTS gestor_incidencias
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE gestor_incidencias;

-- ============================================
-- CREACIÓN DE TABLAS
-- ============================================

-- TABLA USUARIO
CREATE TABLE USUARIO (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'TECNICO', 'CLIENTE') NOT NULL DEFAULT 'CLIENTE',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    INDEX idx_email (email),
    INDEX idx_rol (rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA ESTADOS
CREATE TABLE ESTADOS (
    id_estado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA CATEGORIAS
CREATE TABLE CATEGORIAS (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA INCIDENCIAS
CREATE TABLE INCIDENCIAS (
    id_incidencia INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    prioridad ENUM('Baja', 'Media', 'Alta') NOT NULL DEFAULT 'Media',
    id_usuario_creador INT NOT NULL,
    id_usuario_asignado INT NULL,
    id_estado INT NOT NULL,
    id_categoria INT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP NULL,
    FOREIGN KEY (id_usuario_creador) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario_asignado) REFERENCES USUARIO(id_usuario) ON DELETE SET NULL,
    FOREIGN KEY (id_estado) REFERENCES ESTADOS(id_estado) ON DELETE RESTRICT,
    FOREIGN KEY (id_categoria) REFERENCES CATEGORIAS(id_categoria) ON DELETE RESTRICT,
    INDEX idx_estado (id_estado),
    INDEX idx_categoria (id_categoria),
    INDEX idx_prioridad (prioridad),
    INDEX idx_usuario_creador (id_usuario_creador),
    INDEX idx_usuario_asignado (id_usuario_asignado),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA COMENTARIOS
CREATE TABLE COMENTARIOS (
    id_comentario INT AUTO_INCREMENT PRIMARY KEY,
    mensaje_comentario TEXT NOT NULL,
    id_usuario INT NOT NULL,
    id_incidencia INT NOT NULL,
    fecha_comentario TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_incidencia) REFERENCES INCIDENCIAS(id_incidencia) ON DELETE CASCADE,
    INDEX idx_incidencia (id_incidencia),
    INDEX idx_usuario (id_usuario),
    INDEX idx_fecha (fecha_comentario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLA HISTORIAL DE CAMBIOS
CREATE TABLE HISTORIALCAMBIOS (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    descripcion_cambio TEXT NOT NULL,
    id_incidencia INT NOT NULL,
    id_usuario INT NULL,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_incidencia) REFERENCES INCIDENCIAS(id_incidencia) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE SET NULL,
    INDEX idx_incidencia (id_incidencia),
    INDEX idx_usuario (id_usuario),
    INDEX idx_fecha (fecha_cambio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============
-- VERIFICACIÓN
-- =============
SELECT 'Base de datos y tablas creadas exitosamente' AS Mensaje;
SHOW TABLES;