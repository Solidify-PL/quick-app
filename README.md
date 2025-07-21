
```bash
# latop
# export SOLIDIFY_QA_REPO=/home/tomasz/work/rhizomind/quick-app/data/qa-repository
# export SOLIDIFY_TEMPLATES_DIR=

# desktop
export SOLIDIFY_QA_REPO=/solidify/solidify/spaces/quickapp/data/qa-repo
export SOLIDIFY_TEMPLATES_DIR=/solidify/solidify/spaces/quickapp/source/templates

qa package-all --templates $SOLIDIFY_TEMPLATES_DIR --output $SOLIDIFY_QA_REPO
qa index --input $SOLIDIFY_QA_REPO
rm -rf ~/.cache/quick-app
qa repo update

qa repo remove solidify
qa repo add solidify file:$SOLIDIFY_QA_REPO
qa repo search solidify
qa repo update
```
