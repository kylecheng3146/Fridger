#!/usr/bin/env python3
import os
import re
import sys

def scan_common_main(project_root):
    common_main_path = os.path.join(project_root, 'composeApp/src/commonMain/kotlin')
    if not os.path.exists(common_main_path):
        print(f"❌ commonMain not found at {common_main_path}")
        return

    red_flags = [
        (r'import\s+android\.', "❌ Android import in commonMain"),
        (r'import\s+java\.', "❌ Java import in commonMain (Use Kotlin types)"),
        (r'import\s+UIKit', "❌ UIKit import in commonMain"),
        (r'Dispatchers\.Main', "⚠️ Hardcoded Dispatchers.Main (Use DispatcherProvider)"),
        (r'Dispatchers\.IO', "⚠️ Hardcoded Dispatchers.IO (Use DispatcherProvider)"),
        (r':\s*Context', "❌ Android Context usage in commonMain"),
        (r':\s*Activity', "❌ Android Activity usage in commonMain"),
        (r':\s*ViewController', "❌ iOS ViewController usage in commonMain"),
        (r'GlobalScope', "❌ GlobalScope usage (Potential leak)"),
    ]

    print(f"🔍 Scanning {common_main_path} for KMP violations...")
    
    issues_found = False
    
    for root, _, files in os.walk(common_main_path):
        for file in files:
            if not file.endswith(".kt"):
                continue
                
            file_path = os.path.join(root, file)
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    lines = content.splitlines()
                    
                    for i, line in enumerate(lines):
                        for pattern, message in red_flags:
                            if re.search(pattern, line):
                                rel_path = os.path.relpath(file_path, project_root)
                                print(f"{message} in {rel_path}:{i+1}")
                                print(f"  > {line.strip()}")
                                issues_found = True
            except Exception as e:
                print(f"Error reading {file_path}: {e}")

    if not issues_found:
        print("✅ No obvious Red Flags found in commonMain.")

def main():
    # Default to current working directory if no argument
    project_root = sys.argv[1] if len(sys.argv) > 1 else "."
    scan_common_main(project_root)

if __name__ == "__main__":
    main()
