<?php
// Check if a file was uploaded
if ($_FILES['log_file']['error'] == UPLOAD_ERR_OK && is_uploaded_file($_FILES['log_file']['tmp_name'])) {
    $uploadDir = 'logs/'; // Folder to save log files
    $uploadFile = $uploadDir . basename($_FILES['log_file']['name']);

    // Move the uploaded file to the destination folder
    if (move_uploaded_file($_FILES['log_file']['tmp_name'], $uploadFile)) {
        echo 'Log file uploaded successfully';
    } else {
        echo 'Failed to upload log file';
    }
} else {
    echo 'No log file provided';
}