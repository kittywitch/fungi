{
  description = "clojure experiments";
  outputs = { self, nixpkgs, clj-nix }: let
    forAllSystems = nixpkgs.lib.genAttrs nixpkgs.lib.systems.flakeExposed;
  in {
    packages = forAllSystems (system: let
      pkgs = nixpkgs.legacyPackages.${system};
      in {
      kusachi = clj-nix.lib.mkCljApp {
        inherit pkgs;
        modules = [
          {
            projectSrc = ./.;
            name = "kusachi";
            main-ns = "kusachi.main";
            nativeImage = {
              enable = false;
              graalvm = pkgs.graalvmPackages.graalvm-ce;
              extraNativeImageBuildArgs = [
                "--initialize-at-build-time=com.fasterxml.jackson."
                "--initialize-at-build-time"
                "--features=clj_easy.graal_build_time.InitClojureClasses"
              ];
            };
          }
        ];
      };
      kusachi-site = with pkgs; stdenv.mkDerivation {
        name = "kusachi-site";
        src = ./.;
        version = "irrelevant";
        buildInputs = [
          self.packages.${system}.kusachi
          clojure
          pandoc
          dart-sass
        ];
        buildPhase = ''
          kusachi generate
        '';
        installPhase = ''
          mkdir -p $out
          cp -r output/* $out/
        '';
      };
    });
    devShells = forAllSystems (system: let
      pkgs = nixpkgs.legacyPackages.${system};
    in {
      default = pkgs.mkShell {
        packages = with pkgs; [
          clojure
          ispell
          babashka
          nixd
          dart-sass
          clojure-lsp
          pandoc
          leiningen
          postgresql
          cargo
        ];
      };
    });
  };
  inputs = {
    nixpkgs = {
      url = "github:nixos/nixpkgs/nixos-unstable";
    };
    clj-nix.url = "github:jlesquembre/clj-nix";
  };
}
