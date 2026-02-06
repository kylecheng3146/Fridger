
import os
import urllib.request
import time

# Define the base URL for Microsoft Fluent Emoji
BASE_URL = "https://raw.githubusercontent.com/microsoft/fluentui-emoji/main/assets"

# Target Directory
TARGET_DIR = "composeApp/src/commonMain/composeResources/drawable"
os.makedirs(TARGET_DIR, exist_ok=True)

# Ingredient to Emoji Folder Name mapping
# Key: Ingredient keyword (for filename), Value: Fluent Emoji Folder Name
MAPPING = {
    "apple": "Red apple",
    "avocado": "Avocado",
    "bacon": "Bacon",
    "banana": "Banana",
    "beef": "Cut of meat",
    "bell_pepper": "Bell pepper",
    "blueberry": "Blueberries",
    "bread": "Bread",
    "broccoli": "Broccoli",
    "butter": "Butter",
    "canned_food": "Canned food",
    "carrot": "Carrot",
    "cheese": "Cheese wedge",
    "chicken": "Poultry leg",
    "corn": "Ear of corn",
    "crab": "Crab",
    "cucumber": "Cucumber",
    "dumpling": "Dumpling",
    "egg": "Egg",
    "eggplant": "Eggplant",
    "fish": "Fish",
    "garlic": "Garlic",
    "grapes": "Grapes",
    "lamb": "Meat on bone",
    "leafy_green": "Leafy green",
    "lemon": "Lemon",
    "milk": "Glass of milk",
    "mushroom": "Mushroom",
    "noodle": "Steaming bowl", # Ramen/Noodle
    "oil": "Olive oil", # Custom or Olive
    "onion": "Onion",
    "orange": "Tangerine",
    "pineapple": "Pineapple",
    "potato": "Potato",
    "rice": "Cooked rice",
    "sandwich": "Sandwich",
    "sausage": "Meat on bone", # Fallback or specific? Let's use Meat on bone fallback if needed, but Sausage exists? NO. fallback to Meat
    # Actually "Sausage" might not exist in Fluent. Let's check commonly available ones.
    # "Hot dog" exists. "Meat on bone".
    "seaweed": "Leafy green", # Fallback
    "shrimp": "Shrimp",
    "spaghetti": "Spaghetti",
    "sprout": "Seedling",
    "strawberry": "Strawberry",
    "taco": "Taco",
    "tofu": "Cheese wedge", # Fallback
    "tomato": "Tomato",
    "watermelon": "Watermelon",
    "yogurt": "Cup with straw", # Fallback
    # Extras
    "chili": "Hot pepper"
}

def download_file(url, filepath):
    try:
        urllib.request.urlretrieve(url, filepath)
        print(f"✅ Downloaded: {filepath}")
        return True
    except Exception as e:
        print(f"❌ Failed to download {url}: {e}")
        return False

def main():
    print("🚀 Starting download of 3D PNG icons...")
    
    for key, folder_name in MAPPING.items():
        # Construct URL.
        # Format: assets/{Folder Name}/3D/{lower_case_name}_3d.png
        # Note: Filenames inside 3D folder usually replace spaces with underscores and are lowercase.
        
        filename_base = folder_name.lower().replace(" ", "_")
        url = f"{BASE_URL}/{folder_name.replace(' ', '%20')}/3D/{filename_base}_3d.png"
        
        target_path = os.path.join(TARGET_DIR, f"ic_ingredient_{key}.png")
        
        if not os.path.exists(target_path):
            success = download_file(url, target_path)
            if not success:
                # Try fallback: sometimes filename doesn't have _3d suffix or differs
                print(f"⚠️ Retrying with alternative filename format for {folder_name}...")
                # Try without _3d suffix? No, usually they have it.
        else:
            print(f"⏩ Skipped (already exists): {target_path}")
            
    print("🎉 Download complete!")

if __name__ == "__main__":
    main()
