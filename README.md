# CamXApp

Application Android de démonstration utilisant CameraX pour la capture de photos avec des fonctionnalités avancées.

## Fonctionnalités
- Capture de photos (simple et rafale).
- Zoom par pincement (Pinch-to-zoom).
- Validation du numéro de conteneur.
- Intégration Firebase (Analytics & Crashlytics).
- Tests d'instrumentation avec Espresso et Idling Resources.

## Installation et Configuration

### Prérequis
- Android Studio Ladybug (ou version plus récente).
- SDK Android 23 (Minimum) / 36 (Compile).
- Un compte Firebase (pour Analytics/Crashlytics).

### Étapes d'installation
1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/votre-compte/CamXApp.git
   ```
2. **Configuration Firebase :**
   - Téléchargez votre fichier `google-services.json` depuis la console Firebase.
   - Placez-le dans le répertoire `app/`.
3. **Configuration de la Licence (Optionnel) :**
   - Pour utiliser le service de licence Google Play, ajoutez votre clé publique dans le fichier `local.properties` à la racine du projet :
     ```properties
     PLAY_CONSOLE_PUBLIC_KEY="VOTRE_CLE_RSA_BASE64"
     ```
4. **Synchroniser et Lancer :**
   - Ouvrez le projet dans Android Studio.
   - Cliquez sur "Sync Project with Gradle Files".
   - Lancez l'application sur un appareil physique ou un émulateur.

## Tests
Pour exécuter les tests d'instrumentation (Espresso) :
```bash
./gradlew connectedDebugAndroidTest
```

## Confidentialité
Votre vie privée est importante pour nous. Pour plus d'informations sur la manière dont nous traitons vos données, veuillez consulter notre [Politique de Confidentialité](PRIVACY_POLICY.md).

## Licence
Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.
