# Final test data loading script
$baseUrl = "http://localhost:8080/api"

# Get admin token
$token = (Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/login" -ContentType "application/json" -Body '{"username":"admin","password":"admin"}').token
$headers = @{ Authorization = "Bearer $token" }

Write-Host "Loading test defects..." -ForegroundColor Yellow

# Create test defects without due dates
$testDefects = @(
    @{ title = "Foundation Crack"; description = "Crack 2mm wide found in foundation slab"; priority = "HIGH"; status = "NEW"; projectId = 2; assigneeId = 4 },
    @{ title = "Incorrect Brick Laying"; description = "Violation of brick laying technology on 3rd floor"; priority = "MEDIUM"; status = "IN_PROGRESS"; projectId = 2; assigneeId = 5 },
    @{ title = "Electrical Wiring Issues"; description = "Poor quality electrical wiring in basement"; priority = "HIGH"; status = "NEW"; projectId = 3; assigneeId = 4 },
    @{ title = "Insufficient Insulation"; description = "Additional wall insulation required"; priority = "LOW"; status = "REVIEW"; projectId = 4; assigneeId = 5 },
    @{ title = "Ventilation Problems"; description = "Insufficient ventilation system capacity"; priority = "MEDIUM"; status = "CLOSED"; projectId = 5; assigneeId = 4 },
    @{ title = "Roof Defect"; description = "Leak in roof above assembly hall"; priority = "HIGH"; status = "IN_PROGRESS"; projectId = 5; assigneeId = 5 },
    @{ title = "Incorrect Window Installation"; description = "Windows installed with technology violations"; priority = "MEDIUM"; status = "NEW"; projectId = 3; assigneeId = 4 },
    @{ title = "Sewage Problems"; description = "Blockage in sewage system"; priority = "LOW"; status = "REVIEW"; projectId = 4; assigneeId = 5 }
)

$createdDefects = 0
foreach ($defect in $testDefects) {
    try {
        Write-Host "  Creating defect: $($defect.title)" -ForegroundColor Cyan
        $defectResponse = Invoke-RestMethod -Method Post -Uri "$baseUrl/defects" -ContentType "application/json" -Headers $headers -Body ($defect | ConvertTo-Json)
        Write-Host "  Defect '$($defect.title)' created with ID: $($defectResponse.id)" -ForegroundColor Green
        $createdDefects++
    } catch {
        Write-Host "  Error creating defect '$($defect.title)': $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nTest data loading completed!" -ForegroundColor Green
Write-Host "Defects created: $createdDefects" -ForegroundColor Cyan
Write-Host "Open http://localhost:5173 to view the system" -ForegroundColor Yellow

Write-Host "`nTest accounts:" -ForegroundColor Yellow
Write-Host "  admin/admin - Administrator" -ForegroundColor White
Write-Host "  engineer1/password123 - Engineer 1" -ForegroundColor White
Write-Host "  engineer2/password123 - Engineer 2" -ForegroundColor White
Write-Host "  manager1/password123 - Manager" -ForegroundColor White
Write-Host "  viewer1/password123 - Viewer" -ForegroundColor White
