{
  description = "clojure experiments";
  outputs = { self, nixpkgs, systems }: let
    forAllSystems = nixpkgs.lib.genAttrs (import systems);
  in {
    devShells = forAllSystems (system: let
      pkgs = nixpkgs.legacyPackages.${system};
    in {
      default = pkgs.mkShell {
        packages = with pkgs; [
          clojure
          ispell
          nixd
          leiningen
          postgresql
        ];
        shellHook = ''
    set -e
    export PGDIR=''${PROJECT_ROOT}/postgres
    export PGHOST=$PGDIR
    export PGDATA=$PGDIR/data
    export PGLOG=$PGDIR/log

    if test ! -d $PGDIR; then
      mkdir $PGDIR
    fi

   if [ ! -d $PGDATA ]; then
     echo 'Initializing postgresql database...'
     initdb $PGDATA --auth=trust >/dev/null
   fi
        '';
      };
    });
  };
  inputs = {
    nixpkgs = {
      url = "github:nixos/nixpkgs/nixos-unstable";
    };
    systems.url = "github:nix-systems/default";
  };
}
