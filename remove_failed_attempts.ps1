$files = get-ChildItem "server_logs/**/*.log"
$pwd_length = (get-location).tostring().length

write-host "Removing failed files..."
$removed = 0
ForEach($file in $files){
    $solved = get-content $file | Select-String "#solved" -Context 0,1
    if($solved -match "false"){
        write-host $file.fullname.substring($pwd_length)
        remove-item $file
        $removed += 1
    }
}
write-host "Finished. Removed $removed files."
