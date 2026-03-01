{
  pkgs,
  ...
}:
{
  # https://devenv.sh/languages/
  languages.javascript = {
    enable = true;
    npm.enable = true;
  };

  # https://devenv.sh/scripts/
  scripts.gsd-opencode.exec = "npx gsd-opencode \"$@\"";

  enterShell = ''
    echo "Welcome to the gsd-opencode development environment!"
    echo "You can run 'gsd-opencode' directly."
  '';

  # See full reference at https://devenv.sh/reference/options/
}
