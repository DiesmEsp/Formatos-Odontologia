Utilidades transversales de la aplicacion:

- `LogConfig.java`     Configura java.util.logging (JUL) para escribir a un archivo
                      en la carpeta de datos de la app (%APPDATA%\FormatosOdontologia).
- `SqlErrorUtil.java` Traduce SQLException a mensajes entendibles para el usuario
                      (unicidad, FK, NOT NULL, CHECK, base ocupada, tabla inexistente).
- `ExportErrorUtil.java` Genera mensajes claros + sugerencia para errores de
                      exportacion (archivo ocupado, permisos, ubicacion invalida).
