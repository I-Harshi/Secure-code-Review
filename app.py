import os
import subprocess
from flask import Flask, render_template, request, redirect, url_for

app = Flask(__name__)

UPLOAD_FOLDER = 'uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

@app.route('/')
def upload_file():
    return '''
    <!doctype html>
    <html>
    <head><title>Upload Java File</title></head>
    <body>
        <h1>Upload your Java program file</h1>
        <form action="/display" method="post" enctype="multipart/form-data">
            <input type="file" name="file" accept=".java" required>
            <input type="submit" value="Upload">
        </form>
    </body>
    </html>
    '''

@app.route('/display', methods=['POST'])
def display_file():
    if 'file' not in request.files:
        return redirect(url_for('upload_file'))

    file = request.files['file']
    
    if file.filename == '':
        return redirect(url_for('upload_file'))

    if not file.filename.endswith('.java'):
        return '''
        <!doctype html>
        <html>
        <head><title>Error</title></head>
        <body>
            <h1>Error: The uploaded file is not a .java file</h1>
            <a href="/">Try Again</a>
        </body>
        </html>
        '''

    file_path = os.path.join(UPLOAD_FOLDER, file.filename)
    file.save(file_path)
    
    # Call the Java program to check for vulnerabilities
    try:
        result = subprocess.run(['java', '-jar', 'VC.jar', file_path],
                                capture_output=True, text=True, check=True)
        vulnerability_report = result.stdout
    except subprocess.CalledProcessError as e:
        vulnerability_report = f"Error occurred while checking for vulnerabilities:\n{e.stderr}"

    return f'''
    <!doctype html>
    <html>
    <head><title>Vulnerability Report</title></head>
    <body>
        <h1>Vulnerability Report for {file.filename}</h1>
        <pre>{vulnerability_report}</pre>
        <a href="/">Upload another file</a>
    </body>
    </html>
    '''

if __name__ == '__main__':
    app.run(debug=True)
