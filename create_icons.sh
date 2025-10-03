#!/bin/bash
# Generate PNG icons from vector drawable using ImageMagick or similar
# For now, we'll create simple placeholder PNGs

# Create simple colored squares as placeholders for PNG icons
convert -size 48x48 xc:"#1a1a2e" /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-mdpi/ic_launcher.png
convert -size 72x72 xc:"#1a1a2e" /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-hdpi/ic_launcher.png
convert -size 96x96 xc:"#1a1a2e" /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert -size 144x144 xc:"#1a1a2e" /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert -size 192x192 xc:"#1a1a2e" /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png

# Create round versions
cp /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-mdpi/ic_launcher.png /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-mdpi/ic_launcher_round.png
cp /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-hdpi/ic_launcher.png /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-hdpi/ic_launcher_round.png
cp /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xhdpi/ic_launcher.png /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xhdpi/ic_launcher_round.png
cp /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xxhdpi/ic_launcher.png /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png
cp /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png /home/ubuntu/copilot-android-client/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png

echo "Icon placeholders created"