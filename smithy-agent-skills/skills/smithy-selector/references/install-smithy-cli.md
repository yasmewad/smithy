# Installing the Smithy CLI

## MacOS

### Homebrew (Recommended)
```bash
brew tap smithy-lang/tap && brew install smithy-cli
```

### Manual (x86)
```bash
mkdir -p smithy-install/smithy && \
  curl -L https://github.com/smithy-lang/smithy/releases/latest/download/smithy-cli-darwin-x86_64.zip \
    -o smithy-install/smithy-cli-darwin-x86_64.zip && \
  unzip -qo smithy-install/smithy-cli-darwin-x86_64.zip -d smithy-install && \
  mv smithy-install/smithy-cli-darwin-x86_64/* smithy-install/smithy && \
  sudo smithy-install/smithy/install && \
  rm -rf smithy-install/
```

### Manual (ARM / Apple Silicon)
```bash
mkdir -p smithy-install/smithy && \
  curl -L https://github.com/smithy-lang/smithy/releases/latest/download/smithy-cli-darwin-aarch64.zip \
    -o smithy-install/smithy-cli-darwin-aarch64.zip && \
  unzip -qo smithy-install/smithy-cli-darwin-aarch64.zip -d smithy-install && \
  mv smithy-install/smithy-cli-darwin-aarch64/* smithy-install/smithy && \
  sudo smithy-install/smithy/install && \
  rm -rf smithy-install/
```

## Linux

### Manual (x86)
```bash
mkdir -p smithy-install/smithy && \
  curl -L https://github.com/smithy-lang/smithy/releases/latest/download/smithy-cli-linux-x86_64.zip \
    -o smithy-install/smithy-cli-linux-x86_64.zip && \
  unzip -qo smithy-install/smithy-cli-linux-x86_64.zip -d smithy-install && \
  mv smithy-install/smithy-cli-linux-x86_64/* smithy-install/smithy && \
  sudo smithy-install/smithy/install && \
  rm -rf smithy-install/
```

### Manual (ARM)
```bash
mkdir -p smithy-install/smithy && \
  curl -L https://github.com/smithy-lang/smithy/releases/latest/download/smithy-cli-linux-aarch64.zip \
    -o smithy-install/smithy-cli-linux-aarch64.zip && \
  unzip -qo smithy-install/smithy-cli-linux-aarch64.zip -d smithy-install && \
  mv smithy-install/smithy-cli-linux-aarch64/* smithy-install/smithy && \
  sudo smithy-install/smithy/install && \
  rm -rf smithy-install/
```

## Windows

### Scoop (Recommended)
```powershell
scoop bucket add smithy-lang https://github.com/smithy-lang/scoop-bucket
scoop install smithy-lang/smithy-cli
```

## Documentation

Full installation guide: https://smithy.io/2.0/guides/smithy-cli/cli_installation.html

## Verify Installation
```bash
smithy --help
```
