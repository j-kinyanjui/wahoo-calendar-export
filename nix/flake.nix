{
  description = "Wahoo Plan to Calendar — CLI tool for exporting Wahoo SYSTM training plans to .ics";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        src = ./..;
      in
      {
        packages = {
          default = pkgs.stdenv.mkDerivation {
            pname = "wahoo-cli";
            version = "1.0.0";
            inherit src;

            nativeBuildInputs = [
              pkgs.gradle
              pkgs.jdk17
              pkgs.makeWrapper
            ];

            buildInputs = [ pkgs.jdk17 ];

            # Gradle needs a writable home for caches
            buildPhase = ''
              export GRADLE_USER_HOME=$(mktemp -d)
              gradle installDist -x test --no-daemon
            '';

            installPhase = ''
              mkdir -p $out
              cp -r build/install/wahoo-plan-to-calendar/* $out/

              # Patch the start script to use the Nix JRE
              substituteInPlace $out/bin/wahoo-plan-to-calendar \
                --replace-warn '#!/usr/bin/env sh' '#!${pkgs.bash}/bin/bash'

              wrapProgram $out/bin/wahoo-plan-to-calendar \
                --set JAVA_HOME "${pkgs.jre_minimal}"
            '';

            meta = {
              description = "CLI tool to fetch Wahoo SYSTM training plans and export as .ics calendar files";
              mainProgram = "wahoo-plan-to-calendar";
            };
          };
        };
      }
    );
}
