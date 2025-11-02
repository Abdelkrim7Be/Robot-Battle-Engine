This shared project is intended to contain the jar files produced by the different teams, which will be loaded into a battle
to allow robots from different teams to compete against each other.

It should be included in your project as a GIT submodule

## Some commands to manage GIT submodules

### Include this shared project in your project as a submodule

Add a submodule in your project

    git submodule add <shared_project_url> 

example:

    git submodule add git@forgens.univ-ubs.fr:pooa/project/libs.git

Or clone a project with submodules

    git clone --recurse-submodules <project_url>

example:

    git submodule add git@forgens.univ-ubs.fr:pooa/project/robots.git

### Updating your submodule after changes in the shared remote project

Pull the remote changes to your local repository

    git submodule update --remote [submodule_name]  # submodule_name is optional

examples: 

    git submodule update --remote libs              # update only the libs submodule
    git submodule update --remote                   # update all submodules

Push this change to the remote repository of your project

    git commit -am "Submodule updated"

### Updating the shared remote project after changes in your submodule 

Nb: the changes have already been done in your submodule

Go in the submodule and create a temporary branch to commit the new changes

    cd <submodule_name>
    git checkout -b tmp_branch
    git commit -am "My small changes"             # adapt the commit message

Merge the temporary branch to the main branch and push the changes to the remote shared project

    git checkout main
    git merge tmp_branch -m "My small changes"    # adapt the commit message
    git push

Go in your base project and commit and push the changes to your project

    cd ..
    git commit -am "My small changes"             # adapt the commit message
    git push

### Some references

* [w3schools](https://www.w3schools.com/git/git_submodules.asp)
* [Git community documentation](https://git-scm.com/book/en/v2/Git-Tools-Submodules)
