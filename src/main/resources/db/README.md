Aqui ira la definicion de la base de datos local.

Diseno previsto a alto nivel:

- `operador`: catalogo de operadores o doctores
- `paciente`: catalogo de pacientes
- `material`: catalogo de materiales e insumos
- `tratamiento`: registro principal de la atencion
- `tratamiento_material`: detalle de materiales usados por tratamiento

Relaciones previstas:

- un `paciente` puede tener muchos `tratamientos`
- un `operador` puede tener muchos `tratamientos`
- un `tratamiento` puede usar muchos `materiales`
- un `material` puede aparecer en muchos `tratamientos`

Cuando se implemente de verdad, aqui ira el `schema.sql` o las migraciones necesarias.
