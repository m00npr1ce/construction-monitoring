# Load test defects
$baseUrl = "http://localhost:8080/api"

# Get admin token
$token = (Invoke-RestMethod -Method Post -Uri "$baseUrl/auth/login" -ContentType "application/json" -Body '{"username":"admin","password":"admin"}').token
$headers = @{ Authorization = "Bearer $token" }

Write-Host "Getting existing users and projects..." -ForegroundColor Yellow

# Get existing users
$users = Invoke-RestMethod -Method Get -Uri "$baseUrl/users" -Headers $headers
$userIds = @{}
foreach ($user in $users) {
    $userIds[$user.username] = $user.id
    Write-Host "Found user: $($user.username) (ID: $($user.id), Role: $($user.role))" -ForegroundColor Cyan
}

# Get existing projects
$projects = Invoke-RestMethod -Method Get -Uri "$baseUrl/projects" -Headers $headers
$projectIds = @{}
foreach ($project in $projects) {
    $projectIds[$project.name] = $project.id
    Write-Host "Found project: $($project.name) (ID: $($project.id))" -ForegroundColor Cyan
}

# Create test defects
Write-Host "Creating test defects..." -ForegroundColor Yellow

$testDefects = @(
    @{ title = "Foundation Crack"; description = "Crack 2mm wide found in foundation slab"; priority = "HIGH"; status = "NEW"; projectName = "Residential Complex North"; assigneeUsername = "engineer1"; dueDate = "2025-02-15T00:00:00" },
    @{ title = "Incorrect Brick Laying"; description = "Violation of brick laying technology on 3rd floor"; priority = "MEDIUM"; status = "IN_PROGRESS"; projectName = "Residential Complex North"; assigneeUsername = "engineer2"; dueDate = "2025-01-30T00:00:00" },
    @{ title = "Electrical Wiring Issues"; description = "Poor quality electrical wiring in basement"; priority = "HIGH"; status = "NEW"; projectName = "Shopping Center Mega"; assigneeUsername = "engineer1"; dueDate = "2025-03-01T00:00:00" },
    @{ title = "Insufficient Insulation"; description = "Additional wall insulation required"; priority = "LOW"; status = "REVIEW"; projectName = "Office Building Business Plaza"; assigneeUsername = "engineer2"; dueDate = "2025-02-28T00:00:00" },
    @{ title = "Ventilation Problems"; description = "Insufficient ventilation system capacity"; priority = "MEDIUM"; status = "CLOSED"; projectName = "School No.15"; assigneeUsername = "engineer1"; dueDate = "2025-01-15T00:00:00" },
    @{ title = "Roof Defect"; description = "Leak in roof above assembly hall"; priority = "HIGH"; status = "IN_PROGRESS"; projectName = "School No.15"; assigneeUsername = "engineer2"; dueDate = "2025-02-10T00:00:00" },
    @{ title = "Incorrect Window Installation"; description = "Windows installed with technology violations"; priority = "MEDIUM"; status = "NEW"; projectName = "Shopping Center Mega"; assigneeUsername = "engineer1"; dueDate = "2025-03-15T00:00:00" },
    @{ title = "Sewage Problems"; description = "Blockage in sewage system"; priority = "LOW"; status = "REVIEW"; projectName = "Office Building Business Plaza"; assigneeUsername = "engineer2"; dueDate = "2025-02-05T00:00:00" }
)

$createdDefects = 0
foreach ($defect in $testDefects) {
    try {
        Write-Host "  Creating defect: $($defect.title)" -ForegroundColor Cyan
        
        # Find project ID
        $projectId = $projectIds[$defect.projectName]
        if (-not $projectId) {
            Write-Host "  Project '$($defect.projectName)' not found" -ForegroundColor Red
            continue
        }
        
        # Find assignee ID
        $assigneeId = $userIds[$defect.assigneeUsername]
        if (-not $assigneeId) {
            Write-Host "  User '$($defect.assigneeUsername)' not found" -ForegroundColor Red
            continue
        }
        
        $defectData = @{
            title = $defect.title
            description = $defect.description
            priority = $defect.priority
            status = $defect.status
            projectId = $projectId
            assigneeId = $assigneeId
            dueDate = $defect.dueDate
        }
        
        $defectResponse = Invoke-RestMethod -Method Post -Uri "$baseUrl/defects" -ContentType "application/json" -Headers $headers -Body ($defectData | ConvertTo-Json)
        Write-Host "  Defect '$($defect.title)' created with ID: $($defectResponse.id)" -ForegroundColor Green
        $createdDefects++
    } catch {
        Write-Host "  Error creating defect '$($defect.title)': $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Summary:" -ForegroundColor Magenta
Write-Host "Users: $($users.Count)" -ForegroundColor Cyan
Write-Host "Projects: $($projects.Count)" -ForegroundColor Cyan
Write-Host "Defects created: $createdDefects" -ForegroundColor Cyan

Write-Host "Test data loading completed!" -ForegroundColor Green
Write-Host "Open http://localhost:5173 to view the system" -ForegroundColor Yellow
