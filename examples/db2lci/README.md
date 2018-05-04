# db2lci
This is a tool that takes an openLCA unit process database, calculates the LCI
result of each process, and exports it to an JSON-LD package. Currently, this
tool only works with databases where all default providers are set.

## Usage
Build the tool with the `build.bat` script. This will create it in the
`target/dist` folder. Then you can use the tool via:

```bash
run.bat <database name>
```
