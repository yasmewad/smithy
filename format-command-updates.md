# FormatCommand Boolean Option Integration

## Required Code Changes

### 1. FormatCommand.java
**File**: `/home/ANT.AMAZON.COM/yasmewad/smithy/smithy-cli/src/main/java/software/amazon/smithy/cli/commands/FormatCommand.java`

**Line 48** - Add receiver in `execute()` method:
```java
@Override
public int execute(Arguments arguments, Env env) {
    arguments.addReceiver(new Options());
    arguments.addReceiver(new FormatOptions()); // ADD THIS LINE
```

**Line 62** - Update `run()` method to use option:
```java
private int run(Arguments arguments, Env env) {
    FormatOptions formatOptions = arguments.getReceiver(FormatOptions.class); // ADD THIS LINE
    
    if (arguments.getPositional().isEmpty()) {
        throw new CliError("No .smithy model or directory was provided as a positional argument");
    }

    // Add fail-build-on-check logic
    if (formatOptions.isFailBuildOnCheck()) {
        // Your validation logic before formatting
    }
```

### 2. FormatOptions.java
**File**: `/home/ANT.AMAZON.COM/yasmewad/smithy/smithy-cli/src/main/java/software/amazon/smithy/cli/commands/FormatOptions.java`

**Fix line 24** - Correct option name:
```java
if (name.equals("fail-build-on-check") || name.equals("fb")) { // Remove dash from "fb"
```

**Add at end** - Add getter method:
```java
public boolean isFailBuildOnCheck() {
    return failBuildOnCheck;
}
```

## Summary
- FormatOptions already exists with correct structure
- Just needs integration into FormatCommand via `arguments.addReceiver()`
- Add getter method to access the boolean flag
- Option will be available as `--fail-build-on-check` or `-fb`
