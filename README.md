# minecraft-mod-updater
A small java program to download a list of mods from a hosted manifest.

The program downloads a manifest file containing a structured list of all the mods you wish to run. Then one by one downloads or updates each one. A MD5 check is done to verify that the file at the mod URL doesn't change without notice, as well as verifying that the downloaded file is valid.

The manifest file is a simple json list. There is an example below.

## Build

```bash
$ gradle shadowJar
```

The output jar file will be in `build/libs/mod-updater-<version>.jar`

I've tested with gradle 8.6 and jdk 21.

## Usage

Place the .jar file in your minecraft root directory, i.e. the one that contains the mods directory.

Run:

```bash
$ java mod-updater-1.0.jar <your/manifest/url> <your/minecraft/directory>
```

If you've put the jar in the minecraft directory:
```bash
$ java mod-updater-1.0.jar <your/manifest/url> .
```

## Sample manifest json file:
```json
[
    {
        "url":  "https://cdn.modrinth.com/data/HVnmMxH1/versions/D5fox3fg/ComplementaryReimagined_r5.1.1.zip",
        "home-url":  "https://modrinth.com/shader/HVnmMxH1",
        "name":  "Complementary Shaders Reimagined",
        "version":  "5.1.1",
        "filename":  "complementary-shaders-reimagined.zip",
        "md5":  "36b4136593cac57d2f9aa651603b6e0f",
        "destination":  "shaderpacks",
        "server": false,
        "client": true
    },
    {
        "url":  "https://cdn.modrinth.com/data/ZX66K16c/versions/sr2lCWRH/PickUpNotifier-v20.4.2-1.20.4-Fabric.jar",
        "home-url":  "https://modrinth.com/mod/ZX66K16c",
        "name":  "Pick Up Notifier",
        "version":  "20.4.2",
        "filename":  "pick-up-notifier.jar",
        "md5":  "e0bfa2e56dbe238e9271f4942c2ce08c",
        "destination":  "mods",
        "server": false,
        "client": true
    }
]
```
