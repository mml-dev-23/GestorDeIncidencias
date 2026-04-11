-- ============================================
-- SCRIPT DE INSERCIÓN DE DATOS Y PERMISOS
-- GESTOR DE INCIDENCIAS
-- ============================================

USE gestor_incidencias;

-- ============================================
-- INSERTAR USUARIOS 
-- ============================================
INSERT INTO USUARIO (nombre, apellidos, email, password, rol) VALUES 
('Juan', 'Pérez García', 'juan.perez@empresa.com', 
 SHA2(CONCAT('admin123', 'salt_gestor'), 256), 'ADMINISTRADOR'),
('María', 'García López', 'maria.garcia@empresa.com', 
 SHA2(CONCAT('tecnico123', 'salt_gestor'), 256), 'TECNICO'),
('Juan', 'Martínez Ruiz', 'juan.martinez@empresa.com', 
 SHA2(CONCAT('tecnico456', 'salt_gestor'), 256), 'TECNICO'),
('Ana', 'López Sánchez', 'ana.lopez@empresa.com', 
 SHA2(CONCAT('tecnico789', 'salt_gestor'), 256), 'TECNICO'),
('Carlos', 'López Fernández', 'carlos.lopez@empresa.com', 
 SHA2(CONCAT('cliente123', 'salt_gestor'), 256), 'CLIENTE'),
('Laura', 'Martín González', 'laura.martin@empresa.com', 
 SHA2(CONCAT('cliente456', 'salt_gestor'), 256), 'CLIENTE')
ON DUPLICATE KEY UPDATE 
    nombre = VALUES(nombre), 
    apellidos = VALUES(apellidos), 
    password = VALUES(password), 
    rol = VALUES(rol);

-- ============================================
-- INSERTAR ESTADOS
-- ============================================
INSERT INTO ESTADOS (nombre_estado) VALUES 
('Pendiente'),
('En Proceso'),
('Resuelta'),
('Cerrada')
ON DUPLICATE KEY UPDATE 
    nombre_estado = VALUES(nombre_estado);

-- ============================================
-- INSERTAR CATEGORÍAS
-- ============================================
INSERT INTO CATEGORIAS (nombre_categoria) VALUES 
('Software'),
('Hardware'),
('Red'),
('Seguridad'),
('Otro')
ON DUPLICATE KEY UPDATE 
    nombre_categoria = VALUES(nombre_categoria);  
    
-- ============================================
-- INSERTAR INCIDENCIAS DE EJEMPLO
-- ============================================
INSERT INTO INCIDENCIAS (titulo, descripcion, prioridad, id_usuario_creador, id_usuario_asignado, id_estado, id_categoria) VALUES
('Error en aplicación de nóminas', 'El sistema no calcula correctamente las horas extras del personal. Se requiere revisión urgente del módulo de cálculos.', 'Alta', 5, 2, 2, 1),
('Impresora HP no responde', 'La impresora HP LaserJet de la oficina 3 no responde a solicitudes de impresión desde ningún equipo.', 'Media', 6, 3, 1, 2),
('Conexión WiFi intermitente', 'La señal WiFi en la sala de reuniones del segundo piso se cae cada 10-15 minutos.', 'Alta', 5, 4, 1, 3),
('Actualización de antivirus', 'Es necesario actualizar el software antivirus en todos los equipos del departamento de contabilidad.', 'Media', 1, 2, 3, 4),
('Solicitud de Adobe Photoshop', 'Necesito instalación de Adobe Photoshop CC para trabajar en el nuevo proyecto de marketing digital.', 'Baja', 6, NULL, 1, 5),
('Pantalla parpadeante', 'El monitor de mi estación de trabajo parpadea constantamente, dificulta el trabajo.', 'Media', 5, 3, 1, 2),
('Acceso denegado a carpeta compartida', 'No puedo acceder a la carpeta compartida "Proyectos 2024" en el servidor. Error de permisos.', 'Alta', 6, 4, 1, 3);

-- ============================================
-- INSERTAR COMENTARIOS DE EJEMPLO
-- ============================================
INSERT INTO COMENTARIOS (mensaje_comentario, id_usuario, id_incidencia) VALUES
('He revisado el código de la aplicación y encontré el error en el módulo de cálculo de horas extras.', 2, 1),
('Corrección aplicada. En pruebas actualmente.', 2, 1),
('He reiniciado la impresora y actualizado los drivers. Por favor, intenta imprimir de nuevo.', 3, 2),
('El problema persiste. He escalado a soporte del fabricante.', 3, 2),
('Revisando configuración del router y puntos de acceso.', 4, 3),
('Se encontró interferencia con otro dispositivo. Cambiando canal WiFi.', 4, 3),
('Actualización completada en 45 de 50 equipos. Pendientes 5 equipos apagados.', 2, 4),
('El monitor tiene un problema de hardware. Solicitando reemplazo.', 3, 6);

-- ============================================
-- INSERTAR HISTORIAL DE CAMBIOS
-- ============================================
INSERT INTO HISTORIALCAMBIOS (descripcion_cambio, id_incidencia, id_usuario) VALUES
('Incidencia creada por Carlos López Fernández', 1, 5),
('Asignada al técnico: María García López', 1, 1),
('Estado cambiado a: En Progreso', 1, 2),
('Incidencia creada por Laura Martín González', 2, 6),
('Asignada al técnico: Juan Martínez Ruiz', 2, 1),
('Incidencia creada por Carlos López Fernández', 3, 5),
('Asignada al técnico: Ana López Sánchez', 3, 1),
('Incidencia creada por Juan Pérez García', 4, 1),
('Asignada al técnico: María García López', 4, 1),
('Estado cambiado a: En Progreso', 4, 2),
('Estado cambiado a: Resuelta', 4, 2),
('Incidencia creada por Laura Martín González', 5, 6),
('Incidencia creada por Carlos López Fernández', 6, 5),
('Asignada al técnico: Juan Martínez Ruiz', 6, 1),
('Incidencia creada por Laura Martín González', 7, 6),
('Asignada al técnico: Ana López Sánchez', 7, 1);

-- ============================================
-- USUARIOS DE APLICACIÓN CREADOS:
-- ============================================
-- ADMINISTRADOR: juan.perez@empresa.com / admin123
-- TÉCNICO 1: maria.garcia@empresa.com / tecnico123  
-- TÉCNICO 2: juan.martinez@empresa.com / tecnico456
-- TÉCNICO 3: ana.lopez@empresa.com / tecnico789
-- CLIENTE 1: carlos.lopez@empresa.com / cliente123
-- CLIENTE 2: laura.martin@empresa.com / cliente456


-- ============================================
-- VERIFICACIÓN DE DATOS INSERTADOS
-- ============================================
SELECT '===============================' AS '';
SELECT 'VERIFICACIÓN DE DATOS' AS '';
SELECT '===============================' AS '';
 
SELECT CONCAT('Total Usuarios: ', COUNT(*)) AS '' FROM USUARIO;
SELECT CONCAT('  - Administradores: ', COUNT(*)) AS '' FROM USUARIO WHERE rol = 'ADMINISTRADOR';
SELECT CONCAT('  - Técnicos: ', COUNT(*)) AS '' FROM USUARIO WHERE rol = 'TECNICO';
SELECT CONCAT('  - Clientes: ', COUNT(*)) AS '' FROM USUARIO WHERE rol = 'CLIENTE';
 
SELECT CONCAT('Total Estados: ', COUNT(*)) AS '' FROM ESTADOS;
SELECT CONCAT('Total Categorías: ', COUNT(*)) AS '' FROM CATEGORIAS;
SELECT CONCAT('Total Incidencias: ', COUNT(*)) AS '' FROM INCIDENCIAS;
SELECT CONCAT('Total Comentarios: ', COUNT(*)) AS '' FROM COMENTARIOS;
SELECT CONCAT('Total Historial: ', COUNT(*)) AS '' FROM HISTORIALCAMBIOS;
 
-- ============================================
-- MOSTRAR USUARIOS CREADOS
-- ============================================
SELECT 'USUARIOS DE APLICACIÓN CREADOS:' AS '';
SELECT CONCAT(' ', nombre, ' ', apellidos, ' (', email, ') - ', rol) AS 'Usuario' 
FROM USUARIO ORDER BY rol DESC, nombre;
 
SELECT '===================================' AS '';
SELECT 'Datos insertados correctamente' AS Mensaje;
SELECT 'Las contraseñas están hasheadas con SHA256+salt' AS Nota;
SELECT '===================================' AS '';

-- ============================================
-- CREACIÓN DE USUARIOS DE BD (Para conexiones por rol desde la aplicación)
--
-- NOTA: Estos usuarios MySQL implementan seguridad a nivel
-- de base de datos por roles. Actualmente la aplicación
-- se conecta con el usuario 'root' por simplicidad, pero
-- estos usuarios están preparados para una implementación
-- futura que añada una capa adicional de seguridad.
--
-- ROLES IMPLEMENTADOS:
-- - admin_incidencias: Control total del sistema
-- - tecnico_incidencias: Gestión de incidencias y comentarios  
-- - cliente_incidencias: Solo creación de incidencias propias
-- ============================================
-- Eliminar usuarios si existen previamente
DROP USER IF EXISTS 'admin_incidencias'@'localhost';
DROP USER IF EXISTS 'tecnico_incidencias'@'localhost';
DROP USER IF EXISTS 'cliente_incidencias'@'localhost';

-- Crear usuarios de BD especializados por rol
CREATE USER 'admin_incidencias'@'localhost' IDENTIFIED BY 'Admin123!';
CREATE USER 'tecnico_incidencias'@'localhost' IDENTIFIED BY 'Tecnico123!';
CREATE USER 'cliente_incidencias'@'localhost' IDENTIFIED BY 'Cliente123!';


-- ============================================
-- ASIGNACIÓN DE PRIVILEGIOS
-- ============================================

-- ADMINISTRADOR: Control total de la base de datos
GRANT ALL PRIVILEGES ON gestor_incidencias.* TO 'admin_incidencias'@'localhost';
 
-- TÉCNICO: NO puede ver usuarios, solo gestionar incidencias asignadas
-- TÉCNICOS NO PUEDEN VER TABLA USUARIO (solo Admin)
GRANT SELECT, INSERT, UPDATE ON gestor_incidencias.INCIDENCIAS TO 'tecnico_incidencias'@'localhost';
GRANT SELECT, INSERT, UPDATE ON gestor_incidencias.COMENTARIOS TO 'tecnico_incidencias'@'localhost';
GRANT SELECT ON gestor_incidencias.ESTADOS TO 'tecnico_incidencias'@'localhost';
GRANT SELECT ON gestor_incidencias.CATEGORIAS TO 'tecnico_incidencias'@'localhost';
GRANT SELECT, INSERT ON gestor_incidencias.HISTORIALCAMBIOS TO 'tecnico_incidencias'@'localhost';
 
-- CLIENTE: Solo puede crear incidencias propias, comentar y ver estados/categorías
-- CLIENTES NO PUEDEN VER TABLA USUARIO (solo Admin)
GRANT SELECT, INSERT ON gestor_incidencias.INCIDENCIAS TO 'cliente_incidencias'@'localhost';
GRANT SELECT, INSERT ON gestor_incidencias.COMENTARIOS TO 'cliente_incidencias'@'localhost';
GRANT SELECT ON gestor_incidencias.ESTADOS TO 'cliente_incidencias'@'localhost';
GRANT SELECT ON gestor_incidencias.CATEGORIAS TO 'cliente_incidencias'@'localhost';
 
-- Aplicar todos los cambios de permisos
FLUSH PRIVILEGES;

