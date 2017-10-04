Used Clustering: ${title}
Parameters: 
tfidf: ${tfidf?c}
stopWords: ${stopWords?c}
upperCase: ${upperCase?c}
interpretation: ${interpretation?c}
rarityFilter : ${rarityFilter}

|<#list cclusters as c> ComputerCluster ${c?counter}| HumanCluster ${c?counter} <#sep>|</#list>|
|<#list cclusters as c>--- | --- <#sep>|</#list>|
<#list 0 ..< size as entityIndex>
|<#list cclusters as c> ${(c.requirements[entityIndex].titel)!}| ${(hclusters[c_index].requirements[entityIndex].titel)!} <#sep>|</#list>|
</#list>

