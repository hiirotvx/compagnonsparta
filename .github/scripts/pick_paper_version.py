"""Aligne la version de paper-api du pom sur une version reellement publiee.

Le pom cible une version precise ; si elle n'existe pas encore sur
repo.papermc.io, on retombe sur le snapshot 1.21.x le plus recent.
"""
import re
import sys
import urllib.request

META = ("https://repo.papermc.io/repository/maven-public/"
        "io/papermc/paper/paper-api/maven-metadata.xml")

pom = open("pom.xml", encoding="utf-8").read()
m = re.search(
    r"<artifactId>paper-api</artifactId>\s*<version>([^<]+)</version>", pom)
if not m:
    sys.exit("paper-api introuvable dans le pom")
want = m.group(1)

meta = urllib.request.urlopen(META, timeout=60).read().decode("utf-8")
versions = re.findall(r"<version>([^<]+)</version>", meta)

if want in versions:
    print(f"paper-api {want} disponible, pom inchange")
    sys.exit(0)

def patch_level(v):
    parts = v.split("-")[0].split(".")
    return int(parts[2]) if len(parts) > 2 else 0

candidats = [v for v in versions
             if v.startswith("1.21.") and v.endswith("-R0.1-SNAPSHOT")]
if not candidats:
    sys.exit(f"aucun snapshot 1.21.x disponible (demande : {want})")

retenu = max(candidats, key=patch_level)
pom = pom.replace(f"<version>{want}</version>",
                  f"<version>{retenu}</version>", 1)
open("pom.xml", "w", encoding="utf-8").write(pom)
print(f"paper-api {want} absent du depot -> repli sur {retenu}")
