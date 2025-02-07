# Tock Workflow's

## Tock-docker trigger

After compiling TOCK jar's for each pull request opened (at each new commit)
we trigger the build of the docker images present in tock-docker repository.

## Environment variables configuration

Set the following environment variables (not secrets) :
* `TOCK_DOCKER_REPO_OWNER` : The organisation of repo owner (if personal) where your tock-docker repository is located
* `TOCK_DOCKER_REPO_NAME` : name of the repository, probably `tock-docker`
* `TOCK_DOCKER_DOCKER_BUILD_WORKFLOW_ID` : name of the workflow present in tock-docker probably `maven-docker-ci-cd.yml`
* `TOCK_DOCKER_DOCKER_REF`: the branch used for tock-docker workflow, probably `master`.

### Configure secret

You need a fine-grained Github Token that will have privileges to trigger workflow specifically
on tock-docker repository.

For that go to Account > Settings > Developper > Personnal access token > Fine-grained token.
Select the following scopes :
* Repository access : select only the right tock-docker repository
* Actions > Workflows, workflow runs and artifacts : **Read and write**

Save this Github token as `TOCK_DOCKER_GITHUB_PAT` secret on your tock repository.