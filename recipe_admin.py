import os
import json
import subprocess
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import parse_qs, urlparse

PORT = 8080
WORKING_FILE = "recipes_working.json"
PROD_FILE = "app/src/main/assets/recipes.json"

# Ensure working file exists
if not os.path.exists(WORKING_FILE) and os.path.exists(PROD_FILE):
    with open(PROD_FILE, "r", encoding="utf-8") as f:
        data = f.read()
    with open(WORKING_FILE, "w", encoding="utf-8") as f:
        f.write(data)

class AdminHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        parsed_url = urlparse(self.path)
        if parsed_url.path == "/" or parsed_url.path == "":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write(self.get_html().encode("utf-8"))
        elif parsed_url.path == "/api/recipes":
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            with open(WORKING_FILE, "r", encoding="utf-8") as f:
                content = f.read()
            self.wfile.write(content.encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        parsed_url = urlparse(self.path)
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.r500 = self.rfile.read(content_length).decode('utf-8') if content_length > 0 else "{}"

        if parsed_url.path == "/api/save":
            try:
                data = json.loads(body)
                with open(WORKING_FILE, "w", encoding="utf-8") as f:
                    json.dump(data, f, indent=2)
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"status": "success", "message": "Working copy saved successfully!"}).encode("utf-8"))
            except Exception as e:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"status": "error", "message": str(e)}).encode("utf-8"))

        elif parsed_url.path == "/api/publish":
            try:
                # Load working copy, increment version
                with open(WORKING_FILE, "r", encoding="utf-8") as f:
                    data = json.load(f)

                current_version = int(data.get("version", 1))
                new_version = current_version + 1
                data["version"] = new_version

                # Save back to working copy
                with open(WORKING_FILE, "w", encoding="utf-8") as f:
                    json.dump(data, f, indent=2)

                # Copy working copy to production assets file
                with open(PROD_FILE, "w", encoding="utf-8") as f:
                    json.dump(data, f, indent=2)

                # Run git commit and push
                git_status = "Git push successful!"
                try:
                    subprocess.run(["git", "add", PROD_FILE], check=True)
                    subprocess.run(["git", "commit", "-m", f"Publish recipes update v{new_version}"], check=True)
                    subprocess.run(["git", "push"], check=True)
                except Exception as git_err:
                    git_status = f"Files updated successfully, but git push encountered an issue: {str(git_err)}"

                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"status": "success", "message": f"Published v{new_version}! {git_status}"}).encode("utf-8")).encode("utf-8") if False else self.wfile.write(json.dumps({"status": "success", "message": f"Published v{new_version}! {git_status}"}).encode("utf-8"))
            except Exception as e:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"status": "error", "message": str(e)}).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

    def get_html(self):
        return """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>The Goodie Box - Recipe Admin Dashboard</title>
    <style>
        :root {
            --gingerbread: #A35C2A;
            --candy-red: #C23B22;
            --frosting-cream: #F8E9D9;
            --sugar-white: #FFF9F3;
            --cocoa: #4B2E1E;
            --golden-glow: #E2B97F;
        }
        body {
            background-color: var(--sugar-white);
            color: var(--cocoa);
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
        }
        .header {
            background-color: var(--gingerbread);
            color: white;
            padding: 20px;
            border-radius: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .btn {
            background-color: var(--candy-red);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 6px;
            font-weight: bold;
            cursor: pointer;
        }
        .btn:hover { background-color: #8c4c20; }
        .btn-publish { background-color: #2F5496; }
        .btn-publish:hover { background-color: #1d3557; }
        .container { margin-top: 20px; }
        table {
            width: 100%;
            border-collapse: collapse;
            background-color: var(--frosting-cream);
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 12px 16px;
            text-align: left;
            border-bottom: 1px solid var(--golden-glow);
        }
        th { background-color: var(--gingerbread); color: white; }
        tr:hover { background-color: #f3e5d0; }
        .modal {
            display: none;
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.5);
            justify-content: center; align-items: center;
        }
        .modal-content {
            background: var(--sugar-white);
            padding: 30px;
            border-radius: 8px;
            width: 600px;
            max-height: 90vh;
            overflow-y: auto;
            border: 2px solid var(--golden-glow);
        }
        input, textarea, select {
            width: 100%;
            padding: 8px;
            margin: 6px 0 16px 0;
            border: 1px solid var(--golden-glow);
            border-radius: 4px;
            box-sizing: border-box;
            background: var(--frosting-cream);
        }
    </style>
</head>
<body>

    <div class="header">
        <h1>🎁 The Goodie Box - Recipe Admin</h1>
        <div>
            <button class="btn" onclick="openModal()">+ Add New Recipe</button>
            <button class="btn btn-publish" onclick="publishAndPush()">🚀 Publish & Push to GitHub</button>
        </div>
    </div>

    <div class="container">
        <h2>Working Database Recipes</h2>
        <table id="recipeTable">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Prep / Cook</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>

    <!-- Edit/Add Modal -->
    <div id="recipeModal" class="modal">
        <div class="modal-content">
            <h2 id="modalTitle">Add Recipe</h2>
            <input type="hidden" id="recipeId">
            <label>Title:</label>
            <input type="text" id="recipeTitle">
            <label>Category:</label>
            <select id="recipeCategory">
                <option value="Nuts and Snacks">Nuts and Snacks</option>
                <option value="Cookies">Cookies</option>
                <option value="Candies">Candies</option>
                <option value="Fudge">Fudge</option>
                <option value="Bread">Bread</option>
                <option value="Frostings">Frostings</option>
                <option value="Main Dishes">Main Dishes</option>
            </select>
            <label>Description:</label>
            <textarea id="recipeDesc"></textarea>
            <label>Prep Time & Cook Time:</label>
            <div style="display:flex; gap:10px;">
                <input type="text" id="recipePrep" placeholder="Prep Time">
                <input type="text" id="recipeCook" placeholder="Cook Time">
            </div>
            <label>Ingredients (one per line):</label>
            <textarea id="recipeIngredients" rows="5"></textarea>
            <label>Instructions (one per line):</label>
            <textarea id="recipeInstructions" rows="5"></textarea>
            <label>Handling Instructions:</label>
            <textarea id="recipeHandling"></textarea>
            <label>Modifications:</label>
            <textarea id="recipeMods"></textarea>

            <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:20px;">
                <button class="btn" style="background:#888;" onclick="closeModal()">Cancel</button>
                <button class="btn" onclick="saveRecipe()">Save Recipe</button>
            </div>
        </div>
    </div>

    <script>
        let dbData = { version: 1, recipes: [] };

        async function loadRecipes() {
            const res = await fetch('/api/recipes');
            dbData = await res.json();
            renderTable();
        }

        function renderTable() {
            const tbody = document.querySelector('#recipeTable tbody');
            tbody.innerHTML = '';
            dbData.recipes.forEach((r, idx) => {
                tbody.innerHTML += `
                    <tr>
                        <td>${r.id || idx+1}</td>
                        <td><b>${r.title}</b></td>
                        <td>${r.category}</td>
                        <td>${r.prepTime} / ${r.cookTime}</td>
                        <td>
                            <button class="btn" style="padding:4px 8px; font-size:12px;" onclick="editRecipe(${idx})">Edit</button>
                            <button class="btn" style="padding:4px 8px; font-size:12px; background:#c0392b;" onclick="deleteRecipe(${idx})">Delete</button>
                        </td>
                    </tr>
                `;
            });
        }

        function openModal() {
            document.getElementById('recipeId').value = '';
            document.getElementById('recipeTitle').value = '';
            document.getElementById('recipeDesc').value = '';
            document.getElementById('recipePrep').value = '15 mins';
            document.getElementById('recipeCook').value = '30 mins';
            document.getElementById('recipeIngredients').value = '';
            document.getElementById('recipeInstructions').value = '';
            document.getElementById('recipeHandling').value = '';
            document.getElementById('recipeMods').value = '';
            document.getElementById('modalTitle').innerText = 'Add Recipe';
            document.getElementById('recipeModal').style.display = 'flex';
        }

        function closeModal() {
            document.getElementById('recipeModal').style.display = 'none';
        }

        function editRecipe(idx) {
            const r = dbData.recipes[idx];
            document.getElementById('recipeId').value = r.id || (idx+1);
            document.getElementById('recipeTitle').value = r.title;
            document.getElementById('recipeCategory').value = r.category || 'Nuts and Snacks';
            document.getElementById('recipeDesc').value = r.description || '';
            document.getElementById('recipePrep').value = r.prepTime || '15 mins';
            document.getElementById('recipeCook').value = r.cookTime || '30 mins';
            document.getElementById('recipeIngredients').value = (r.ingredients || []).join('\\n');
            document.getElementById('recipeInstructions').value = (r.instructions || []).join('\\n');
            document.getElementById('recipeHandling').value = r.handlingInstructions || '';
            document.getElementById('recipeMods').value = r.modifications || '';
            document.getElementById('modalTitle').innerText = 'Edit Recipe';
            document.getElementById('recipeModal').style.display = 'flex';
        }

        async function saveRecipe() {
            const id = document.getElementById('recipeId').value || String(Date.now());
            const r = {
                id: id,
                title: document.getElementById('recipeTitle').value,
                description: document.getElementById('recipeDesc').value,
                category: document.getElementById('recipeCategory').value,
                prepTime: document.getElementById('recipePrep').value,
                cookTime: document.getElementById('recipeCook').value,
                ingredients: document.getElementById('recipeIngredients').value.split('\\n').filter(x => x.trim()),
                instructions: document.getElementById('recipeInstructions').value.split('\\n').filter(x => x.trim()),
                handlingInstructions: document.getElementById('recipeHandling').value,
                modifications: document.getElementById('recipeMods').value
            };

            const existingIdx = dbData.recipes.findIndex(x => x.id === id);
            if (existingIdx >= 0) {
                dbData.recipes[existingIdx] = r;
            } else {
                dbData.recipes.push(r);
            }

            await fetch('/api/save', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(dbData)
            });

            closeModal();
            loadRecipes();
            alert('Saved to working database!');
        }

        async function deleteRecipe(idx) {
            if (confirm('Are you sure you want to delete this recipe?')) {
                dbData.recipes.splice(idx, 1);
                await fetch('/api/save', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(dbData)
                });
                loadRecipes();
            }
        }

        async function publishAndPush() {
            if (confirm('Ready to publish update and push to GitHub?')) {
                const res = await fetch('/api/publish', { method: 'POST' });
                const json = await res.json();
                alert(json.message);
                loadRecipes();
            }
        }

        loadRecipes();
    </script>
</body>
</html>
"""

if __name__ == "__main__":
    server = HTTPServer(('localhost', PORT), AdminHandler)
    print(f"🎁 The Goodie Box Admin Tool running at http://localhost:{PORT}")
    print("Press Ctrl+C to stop.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
