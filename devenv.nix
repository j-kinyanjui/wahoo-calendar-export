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

  # https://devenv.sh/scripts/
  scripts.gsd-opencode.exec = "npx gsd-opencode \"$@\"";

  enterShell = ''
    echo "Welcome to your development environment!"
    echo "To use gsd run 'gsd-opencode' to init."
  '';
}
