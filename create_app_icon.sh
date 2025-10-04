#!/bin/bash

# Create GitHub Copilot CLI Android App Icon
# A modern, recognizable icon with robot/AI theme

# Colors
BG_COLOR="#1f2937"  # Dark gray-blue
MAIN_COLOR="#60a5fa"  # Light blue
ACCENT_COLOR="#34d399"  # Green accent

# Function to create icon at specific size
create_icon() {
    local size=$1
    local output=$2
    
    convert -size ${size}x${size} xc:"$BG_COLOR" \
        \( -size $((size-20))x$((size-20)) xc:none \
           -fill "$MAIN_COLOR" \
           -draw "roundrectangle 10,10 $((size-10)),$((size-10)) $((size/8)),$((size/8))" \
        \) -composite \
        \( -size $((size/3))x$((size/3)) xc:none \
           -fill "$ACCENT_COLOR" \
           -draw "circle $((size/6)),$((size/6)) $((size/6)),$((size/12))" \
           -geometry +$((size/4))+$((size/4)) \
        \) -composite \
        \( -size $((size/4))x$((size/8)) xc:none \
           -fill "$ACCENT_COLOR" \
           -draw "roundrectangle 0,0 $((size/4)),$((size/8)) $((size/16)),$((size/16))" \
           -geometry +$((size/2))+$((size*3/5)) \
        \) -composite \
        \( -size $((size/6))x$((size/6)) xc:none \
           -fill "$ACCENT_COLOR" \
           -draw "circle $((size/12)),$((size/12)) $((size/12)),$((size/24))" \
           -geometry +$((size*2/3))+$((size/3)) \
        \) -composite \
        "$output"
}

# Create icons for all densities
echo "Creating app icons..."

# MDPI (48x48)
create_icon 48 "app/src/main/res/mipmap-mdpi/ic_launcher.png"
create_icon 48 "app/src/main/res/mipmap-mdpi/ic_launcher_round.png"

# HDPI (72x72)  
create_icon 72 "app/src/main/res/mipmap-hdpi/ic_launcher.png"
create_icon 72 "app/src/main/res/mipmap-hdpi/ic_launcher_round.png"

# XHDPI (96x96)
create_icon 96 "app/src/main/res/mipmap-xhdpi/ic_launcher.png"
create_icon 96 "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png"

# XXHDPI (144x144)
create_icon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher.png"
create_icon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png"

# XXXHDPI (192x192)
create_icon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
create_icon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"

echo "App icons created successfully!"
echo "Icon theme: Modern GitHub Copilot CLI with robot/AI elements"
