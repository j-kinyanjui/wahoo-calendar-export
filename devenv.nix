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

  treefmt = {
    enable = true;
    config = {
      programs.ktfmt.enable = true;
      settings.formatter.ktfmt = {
        options = [ "--kotlinlang-style" ];
        excludes = [
          "src/**/AuthService.kt"
          "src/**/PlansService.kt"
        ];
      };
    };
  };

  git-hooks.hooks.treefmt.enable = true;

  enterTest = "./gradlew test";

  # https://devenv.sh/scripts/
  scripts.gsd-opencode.exec = "npx gsd-opencode \"$@\"";

  enterShell = ''
    echo "Welcome to your development environment!"
    echo "To use gsd run 'gsd-opencode' to init."
  '';
}
