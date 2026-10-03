$OutputFile = "codebase.txt"
Clear-Content $OutputFile -ErrorAction SilentlyContinue

# Trouve tous les fichiers Java, le POM et les propriétés
$files = Get-ChildItem -Path .\src\main\java\ -Recurse -Filter *.java
$files += Get-Item .\pom.xml -ErrorAction SilentlyContinue
$files += Get-Item .\src\main\resources\application.properties -ErrorAction SilentlyContinue
$files += Get-Item .\docker-compose.yml -ErrorAction SilentlyContinue

foreach ($file in $files) {
    if ($file -ne $null) {
        $relativePath = $file.FullName.Replace((Get-Location).Path + '\', '')
        Add-Content $OutputFile "##### Fichier : $relativePath"
        Get-Content $file.FullName | Add-Content $OutputFile
    }
}
Write-Host "Le fichier codebase.txt a été mis à jour avec succès !" -ForegroundColor Green