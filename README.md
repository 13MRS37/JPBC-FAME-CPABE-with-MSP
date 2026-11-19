# FAME – CP-ABE Implementation

A Java implementation of **FAME (Fast Attribute-based Message Encryption)**, a Ciphertext-Policy Attribute-Based Encryption (CP-ABE) scheme, developed and extended as part of a Master's thesis in Cryptography and Cybersecurity.

The project focuses on the implementation of the FAME cryptographic construction, the parsing of Boolean access policies, their conversion into Monotone Span Programs (MSPs), and the experimental evaluation of the resulting implementation.

## Overview

FAME is a CP-ABE scheme in which access policies are embedded in ciphertexts, while users' secret keys are associated with sets of attributes.

This implementation provides:

- FAME-based CP-ABE encryption and decryption.
- Boolean access-policy parsing.
- Support for `AND` and `OR` policy operators.
- Conversion of access policies into MSP representations.
- MSP reconstruction procedures over the underlying finite field.
- Symmetric encryption support through an AES-based component.
- Benchmarking utilities for evaluating cryptographic operation times.
- Unit and integration tests for selected implementation components.

**Project scope:** This repository contains the Java implementation. Other CP-ABE implementations used for comparative experiments are maintained separately.

## Architecture

The source code is organized into four main packages:

| Package      | Responsibility                                                                                |
| ------------ | --------------------------------------------------------------------------------------------- |
| `mrs.crypto` | FAME cryptographic operations, key structures, ciphertext handling, and AES-based encryption. |
| `mrs.msp`    | MSP construction, matrix representation, context management, and secret reconstruction.       |
| `mrs.policy` | Boolean policy grammar, parsing, and policy-tree construction.                                |
| `mrs.tests`  | Unit tests, integration tests, and benchmarking utilities.                                    |

### Policy Processing

The policy-processing component uses ANTLR to parse Boolean access policies.

The parsed policy is represented as a tree and subsequently compiled into a Monotone Span Program (MSP).

The current policy implementation includes:

- Attribute nodes.
- Binary policy nodes.
- AND nodes.
- OR nodes.
- Policy parsing and visitor components.
- MSP compilation.

The resulting MSP representation is used by the cryptographic implementation to evaluate whether a user's attribute set satisfies the ciphertext access policy.

## Requirements

The project requires:

- Java Development Kit (JDK).
- Java compiler (`javac`).
- JPBC and the required pairing-related dependencies.
- ANTLR runtime and generated parser classes.
- The dependency JAR files included in the `lib/` directory.

The pairing configuration file `MNT224` must remain accessible from the application's working directory, since it is used to initialize the pairing parameters.

## Build

To compile the project, open PowerShell in the project root directory and execute:

```powershell
javac -cp "lib/*" -d out (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

The compiled `.class` files are placed in the `out/` directory.

## Running

After successful compilation, execute the main application using:

```powershell
java -cp "lib/*;out" mrs.crypto.Main
```

The main application is implemented in `Main.java`, under the `mrs.crypto` package.

**Note:** The commands above use the Windows classpath separator (`;`).

## JNA Warning – Native Access

When running the project or its test classes, the Java runtime may display a warning similar to the following:

```text
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::loadLibrary has been called by com.sun.jna.Native
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning
```

This warning is related to the Java Native Access (JNA) library and the native library loading mechanism. **It does not indicate an execution failure and does not affect the normal operation of the project.**

The project and its test classes can execute normally despite this warning, provided that the program completes successfully and reports no actual errors.

### Suppressing the Warning

To suppress this warning, add the following JVM option to the execution command:

```powershell
--enable-native-access=ALL-UNNAMED
```

For example, to run the `ZrMSPReconstructionTest` class:

```powershell
java --enable-native-access=ALL-UNNAMED -cp "lib/*;out" mrs.tests.ZrMSPReconstructionTest
```

The same option can be applied to the main application and any test class by placing it immediately after `java` in the corresponding execution command.

## Tests

The project includes tests covering the main implementation components.

To execute the tests individually, use the following commands:

```powershell
java -cp "lib/*;out" mrs.tests.AESCoderSecurityTest

java -cp "lib/*;out" mrs.tests.HKDFTest

java -cp "lib/*;out" mrs.tests.MSPIntegrationTest

java -cp "lib/*;out" mrs.tests.ZrMSPReconstructionTest
```

The current test suite has previously completed with **37 successful assertions/tests** across the four test classes.

## Benchmarking

The repository includes benchmarking functionality for evaluating the execution time of the FAME operations.

The benchmark focuses on the following operations:

- Setup
- Key Generation
- Encryption
- Decryption
- Total execution time

The benchmark implementation supports experiments with AND-based access policies containing different numbers of attributes.

Execution times are measured in milliseconds, and the results can be exported to CSV files for subsequent analysis.

Benchmark results should be interpreted as measurements of this particular implementation and experimental environment. They should not be treated as direct evidence of the theoretical performance of the FAME scheme independently of implementation and platform-specific factors.

## Research Context

This implementation was developed as part of a Master's thesis in Cryptography and Cybersecurity.

Its purpose is to investigate the implementation and experimental performance of CP-ABE, with particular emphasis on the relationship between Boolean access policies, MSP representations, and the underlying FAME cryptographic operations.

## Disclaimer

This project is intended for academic and research purposes.

It should not be considered a production-ready cryptographic library or used to protect sensitive data without an independent security review.

## Author

Developed as part of a Master's thesis in Cryptography and Cybersecurity.

GitHub: [@13MRS37](https://github.com/13MRS37)
