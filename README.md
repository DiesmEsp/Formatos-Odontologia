# Formatos-Odontologia

Estado actual: estructura base del proyecto, sin implementacion funcional.

La idea es usar este repositorio como esqueleto para una aplicacion de escritorio en Java con interfaz grafica, base de datos local por PC y exportacion a Excel.

## Referencia funcional actual

- `REGISTRO_DE_CONSUMOS_VBA.xlsm`: ejemplo del Excel que sirve como referencia para el formato final de salida.

## Estructura propuesta

```text
src/
  main/
    java/
      com/odontologia/formatos/
        config/
        db/
        model/
        repository/
        service/
        export/
        ui/
          controller/
    resources/
      db/
      templates/
      ui/
      styles/
  test/
    java/
      com/odontologia/formatos/
```

## Nota

Por ahora solo se dejaron carpetas y archivos `README.md` para documentar que debe ir en cada lugar. La implementacion se agregara despues, por modulos.
