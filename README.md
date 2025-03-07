# Semantic_terraform_modules

# Terraform Module Versioning Pipeline

This repository contains a Jenkins pipeline for automating the versioning and tagging process of Terraform AWS modules.

## Overview

The pipeline allows for consistent and standardized version management across multiple Terraform AWS modules. It automates the process of incrementing version numbers (following semantic versioning) and creating Git tags, which helps maintain a clear versioning history for infrastructure-as-code components.

## Supported Repositories

The pipeline is configured to work with the following Terraform AWS modules:

- `terraform-aws-vpc` - AWS VPC module
- `terraform-aws-eks` - AWS EKS (Elastic Kubernetes Service) module
- `terraform-aws-s3` - AWS S3 storage module
- `terraform-aws-dns` - AWS DNS configuration module

## Pipeline Features

- **Semantic Versioning**: Supports incrementing major, minor, or patch versions
- **Kubernetes Agent**: Runs in a Kubernetes pod with Git container
- **Parameterized Execution**: Allows customization of various aspects of the tagging process
- **Credential Management**: Securely handles Git credentials
- **Version Calculation**: Automatically determines the next version based on existing tags

## Parameters

The pipeline can be configured with the following parameters:

| Parameter | Description | Default/Options |
|-----------|-------------|-----------------|
| `REPO_NAME` | Repository name to tag | Choices: `oc-terraform-aws-vpc`, `oc-terraform-aws-eks`, `oc-terraform-aws-s3`, `oc-terraform-aws-dns` |
| `REPO_BRANCH` | Branch to tag | Default: `main` |
| `RELEASE_TYPE` | Version increment type | Choices: `patch`, `minor`, `major` |
| `PUSH_TAG` | Whether to push tag to remote | Default: `true` |
| `CREDENTIALS_ID` | Jenkins credentials ID | Default: `OCSA4` |
| `GIT_USER_NAME` | Git user name | Default: `Jenkins CI/CD` |
| `GIT_USER_EMAIL` | Git user email | Default: `xxxxx` |

## Pipeline Stages

The pipeline consists of the following stages:

1. **Setup**: Prepares the Git environment and clones the selected repository
2. **Calculate Version**: Determines the next version number based on the latest tag and selected release type
3. **Create Tag**: Creates a Git tag with the new version and optionally pushes it to the remote repository

## Usage

### Running the Pipeline

1. Navigate to the Jenkins job for this pipeline
2. Click "Build with Parameters"
3. Select the repository to tag from the dropdown
4. Choose the appropriate branch (default: `main`)
5. Select the release type (`patch`, `minor`, or `major`)
6. Decide whether to push the tag to the remote repository
7. Optionally modify the credentials ID, Git user name, and email
8. Click "Build" to execute the pipeline

### Example

To create a minor version update for the VPC module:

1. Set `REPO_NAME` to `oc-terraform-aws-vpc`
2. Set `RELEASE_TYPE` to `minor`
3. Keep other parameters at their default values
4. Run the pipeline

This will increment the minor version (e.g., from `v1.2.3` to `v1.3.0`) and push the new tag to the repository.

## Notes

- If no previous tags exist, the pipeline will start from `v0.0.0`
- The pipeline follows semantic versioning principles:
  - `patch`: For backwards-compatible bug fixes
  - `minor`: For backwards-compatible new features
  - `major`: For incompatible API changes

## Requirements

- Jenkins with Kubernetes plugin
- Access to a Kubernetes cluster
- Git credentials with push access to the repositories

## Security Considerations

- Credentials are handled securely through Jenkins credential store
- Git commands use HTTPS with authentication

by# Cheikh B
