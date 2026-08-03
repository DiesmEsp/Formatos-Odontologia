Modelos del dominio (POJO con getters/setters, uno por tabla del esquema V1).

- `Materiales` -> tabla Materiales
- `Operador` -> tabla Operadores
- `Docente` -> tabla Docentes
- `Paciente` -> tabla Pacientes
- `Unidad` -> tabla Unidad (unidad de tratamiento)
- `TratamientoPredefinido` -> tabla Tratamiento_PRED (plantilla)
- `TratamientoPredefinidoMaterial` -> tabla Materiales_List_PRED
- `Tratamiento` -> tabla Tratamiento
- `TratamientoMaterial` -> tabla Materiales_List
- `Asistencia` -> tabla Asistencia
- `AsistenciaMaterial` -> tabla Materiales_Asistencia
- `UnidadConversion` -> tabla Unidad_Conversion
- `RegistroAnulacion` -> tabla RegistroAnulacion

El mapeo ResultSet -> modelo lo hace cada repositorio (metodo privado rowToModel).
