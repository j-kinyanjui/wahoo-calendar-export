{
  ...
}:
{
  # https://devenv.sh/languages/
  languages = {
    javascript = {
      enable = true;
      npm.enable = true;
    };
    java = {
      enable = true;
    };
  };

  git-hooks.hooks = {
    ktfmt = {
      enable = true;
      name = "ktfmt";
      description = "Format Kotlin source files with ktfmt";
      entry = "./gradlew ktfmtFormat";
      files = "\\.kt$";
      language = "system";
    };
  };

  enterTest = "./gradlew test";

  # https://devenv.sh/scripts/
  scripts.gsd-opencode.exec = "npx gsd-opencode \"$@\"";

  enterShell = ''
    echo "Welcome to your development environment!"
    echo "To use gsd run 'gsd-opencode' to init."
  '';
}
