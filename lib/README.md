# Library Dependencies

This directory should contain the required libraries for building and testing the IMUtilLib project.

Please download the following JAR files and place them in this directory:

### 1. JUnit
*   **File:** `junit.jar`
*   **Version:** 3.8.2
*   **Download URL:** [https://repo1.maven.org/maven2/junit/junit/3.8.2/junit-3.8.2.jar](https://repo1.maven.org/maven2/junit/junit/3.8.2/junit-3.8.2.jar)

    **Important:** The build script expects this file to be named `junit.jar`. If you downloaded `junit-3.8.2.jar`, you will need to create a symbolic link. This typically requires running your command prompt or PowerShell as an administrator.

    *   **For Command Prompt (cmd.exe):**
        ```cmd
        mklink "UtilLib\lib\junit.jar" "UtilLib\lib\junit-3.8.2.jar"
        ```
    *   **For PowerShell:**
        ```powershell
        New-Item -ItemType SymbolicLink -Path "UtilLib\lib\junit.jar" -Target "UtilLib\lib\junit-3.8.2.jar"
        ```

### 2. AspectJ Runtime
*   **File:** `aspectjrt.jar`
*   **Version:** 1.5.4
*   **Download URL:** [https://repo1.maven.org/maven2/org/aspectj/aspectjrt/1.5.4/aspectjrt-1.5.4.jar](https://repo1.maven.org/maven2/org/aspectj/aspectjrt/1.5.4/aspectjrt-1.5.4.jar)

    **Important:** The build script expects this file to be named `aspectjrt.jar`. If you downloaded `aspectjrt-1.5.4.jar`, you will need to create a symbolic link. This typically requires running your command prompt or PowerShell as an administrator.

    *   **For Command Prompt (cmd.exe):**
        ```cmd
        mklink "UtilLib\lib\aspectjrt.jar" "UtilLib\lib\aspectjrt-1.5.4.jar"
        ```
    *   **For PowerShell:**
        ```powershell
        New-Item -ItemType SymbolicLink -Path "UtilLib\lib\aspectjrt.jar" -Target "UtilLib\libspectjrt-1.5.4.jar"
        ```

### 3. AspectJ Tools
*   **File:** `aspectjtools.jar`
*   **Version:** 1.5.4
*   **Download URL:** [https://repo1.maven.org/maven2/org/aspectj/aspectjtools/1.5.4/aspectjtools-1.5.4.jar](https://repo1.maven.org/maven2/org/aspectj/aspectjtools/1.5.4/aspectjtools-1.5.4.jar)

    **Important:** The build script expects this file to be named `aspectjtools.jar`. If you downloaded `aspectjtools-1.5.4.jar`, you will need to create a symbolic link. This typically requires running your command prompt or PowerShell as an administrator.

    *   **For Command Prompt (cmd.exe):**
        ```cmd
        mklink "UtilLib\lib\aspectjtools.jar" "UtilLib\lib\aspectjtools-1.5.4.jar"
        ```
    *   **For PowerShell:**
        ```powershell
        New-Item -ItemType SymbolicLink -Path "UtilLib\lib\aspectjtools.jar" -Target "UtilLib\lib\aspectjtools-1.5.4.jar"
        ```
