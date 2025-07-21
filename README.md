qa package-all --templates /home/tomasz/work/rhizomind/quick-app/sources/templates --output /home/tomasz/work/rhizomind/quick-app/data/qa-repository
qa index --input /home/tomasz/work/rhizomind/quick-app/data/qa-repository
rm -rf ~/.cache/quick-app
qa repo update

qa repo remove solidify
qa repo add solidify file:/home/tomasz/work/rhizomind/quick-app/data/qa-repository
qa repo search solidify
qa repo update
