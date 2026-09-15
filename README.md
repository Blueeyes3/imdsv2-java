# imdsv2-java
a sample package to dump imdsv2 creds

*Note:
If env variables needs to be fetched, use the below code snippet*
```
 String variableName = "ENV_VARIABLE_NAME";
    String variableValue = System.getenv(variableName);

    if (variableValue == null) {
        System.out.println(variableName + " is not set");
    } else {
        System.out.println(variableName + "=" + variableValue);
    }
```
