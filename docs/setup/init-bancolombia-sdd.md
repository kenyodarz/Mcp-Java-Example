# MASTER AGENT INSTRUCTION: Bancolombia SDD Initialization

Greeting Agent. You are operating on a repository that already contains initial structural code.
Your primary task, before parsing any API contract, is to inject Bancolombia's Clean Architecture
baseline knowledge into your active memory and the local workspace.

---

## Step 1: Clone and Inject Rules (MANDATORY - FAIL FAST)

Use your terminal command execution tools (PowerShell or Bash) to clone the public repository
`kenyodarz/bancolombia-api-sdd-framework-export`.

### Step 1.0: Prepare Temporary Workspace (CRITICAL)

You MUST create a unique temporary directory for this execution.

* Generate a unique folder name using timestamp or random suffix:

    * PowerShell: `$tempDir = "$env:TEMP\sdd-framework-$((Get-Date).Ticks)"`
    * Bash: `tempDir="/tmp/sdd-framework-$(date +%s%N)"`

* BEFORE creating the folder:

    * If the directory already exists: DELETE it completely

* Create the directory

* Use this directory as the working folder for cloning

* Log the generated path and reuse it for the entire Step 1 (DO NOT regenerate it)

### Step 1.1: Clone Repository

* Clone the repository into the generated temporary directory:
  ```bash
  git clone https://github.com/kenyodarz/bancolombia-api-sdd-framework-export.git <tempDir>
  ```
* Ensure the clone operation completes successfully
* If clone fails: **STOP execution immediately**

### Step 1.2: Injection

Once downloaded:

* Copy the contents of the following directories into the root of the current project:

    * `docs/` → Copy to `docs/` (merge if exists)
    * `overlays/` → Copy to `overlays/` (create if not exists)
    * `skills/` → Copy to `skills/` (create if not exists)

* DO NOT overwrite existing business files

* DO NOT skip any directory

* Ensure full structural integrity of the copied content

### Step 1.3: Cleanup

* After copying: **DELETE the temporary directory completely**

* If cleanup fails: Report it, but DO NOT block execution

---

## Step 1.4: Mandatory Structure Validation (CRITICAL)

You MUST validate that the framework was injected correctly. This validation is **REQUIRED before
continuing**.

### Required Files

* `docs/playbooks/agent-api-to-mcp-workflow.md` (CRITICAL)
* `docs/playbooks/scaffold-baseline.md`
* `docs/playbooks/security-baseline.md`
* `docs/templates/api-contract-to-mcp.md`
* `docs/templates/feature-spec.md`
* `docs/templates/tasks-checklist.md`
* `docs/templates/traceability-matrix.md`
* `docs/sdd/adoption-guide.md`
* `overlays/api-to-mcp/README.md`
* `overlays/api-to-mcp/SKILL.md`
* `overlays/mcp-security-entra-id/README.md`
* `skills/api-design/SKILL.md`
* `skills/spec-intake/SKILL.md`
* `skills/traceability/SKILL.md`
* `AGENTS.md`

### Required Directories

* `docs/`
* `docs/playbooks/`
* `docs/templates/`
* `docs/sdd/`
* `overlays/`
* `overlays/api-to-mcp/`
* `overlays/mcp-security-entra-id/`
* `skills/`

---

## Step 1.5: Validation Rules

* If **ANY** file or directory is missing:

    * **IMMEDIATELY STOP execution**
    * Report **EXACTLY** which file or directory is missing
    * **DO NOT continue under any circumstances**

* DO NOT assume the injection was successful without explicit validation

* **This step is a HARD BLOCKER for the entire process**

---

## Step 2: Framework Comprehension (MANDATORY)

Immediately read the file:

**`docs/playbooks/agent-api-to-mcp-workflow.md`**

Treat it as your absolute Clean Architecture guideline for the remainder of this session.

---

## Step 2.1: Playbook Integrity Check

* Confirm the file is not empty
* Confirm it contains instructions related to:
    * MCP Tool generation
    * 1:1 mapping between endpoint, UseCase and Adapter
    * 5 phases: Intake, Scaffolding, Refinement, Security, Cleanup

If not:

* **STOP execution immediately**

---

## Step 3: Toolification Execution (Mass MCP Generation)

The user will provide you with a target OpenAPI or Postman JSON file path in their subsequent
message.

Strictly following the Playbook:

* Iterate through **EVERY** HTTP method

* Generate:

    * 1 Model
    * 1 Use Case
    * 1 Adapter
    * 1 MCP Tool

* Maintain strict 1:1 mapping

---

## Global Enforcement Rules

* NO partial execution is allowed
* NO assumptions about missing files
* NO deviations from the injected Playbook
* NO architectural changes are permitted
* **FAIL FAST on any inconsistency**

---

## You may begin.

Execute this initialization process and report:

1. ✓ Framework SDD successfully injected
2. ✓ All required files and directories validated
3. ✓ agent-api-to-mcp-workflow.md comprehended and ready for Phase 1
4. Ready for Step 3 (user provides API contracts)

---

## References

- 📘 [Playbook Detallado](../SDD/playbook-mcp-refactor.md)
- 🤖 [Prompt para Agentes](../SDD/prompt-disparador-mcp.md)
- 📖 [Framework SDD](https://github.com/kenyodarz/bancolombia-api-sdd-framework-export)

