# Propriétés détaillées

L'objectif de cette nouvelle ressource est de fournir la liste complète des propriétés détaillées d'une plateforme. Cela inclut :
* les propriétés globales de la plateforme
* les propriétés de chaque module de la plateforme

Son application directe sera de fournir côté frontend une modale permettant d'avoir une vue d'ensemble des usages des propriétés globales sur une plateforme.

## Contexte

Un première implémentation a permis de récupérer en partie ces données mais avec les inconvénients suivants :
 * 2 appels à l'API *par module* (les propriétés valorisées puis le modèle de propriétés)
 * Implémentation de la logique de fusion de ces 2 appels dans le frontend
 
Avec cette première implémentation, l'affichage du détail des propriétés de plateformes volumineuses peut prendre plusieurs dizaines de secondes, voire tomber en timeout.

La solution proposée ici permet de récupérer au choix l'ensemble des données d'une platforme ou d'un module spécifique, en un seul appel qui correspond à 2 requêtes sur la base de données au total.

Le temps de traitement constaté sur une plateforme volumineuse est de l'ordre de quelques secondes.

## Endpoint

    GET /applications/{application_name}/platforms/{platform_name}/detailed_properties?properties_path={properties_path}

Le paramètre de requête `properties_path` est facultatif. S'il est fourni, on ne retourne que les propriétés du module concerné.

## Structure

    {
        application_name,
        platform_name,
        global_properties: [
            {
                name,
                stored_value,
                final_value
            },
            ...
        ],
        deployed_modules: [
            {
                name,
                version,
                version_type,
                module_path,
                properties_path,
                detailed_properties: [
                    {
                        name,
                        stored_value,
                        final_value,
                        default_value,
                        is_required,
                        is_password,
                        pattern,
                        comment,
                        referenced_global_properties: [
                            {
                                name,
                                stored_value,
                                final_value
                            },
                            ...
                        ],
                        referencing_modules: [
                            {
                                name,
                                version,
                                version_type
                            },
                            ...
                        ],
                        is_not_used,
                    },
                    ...
                ]
            }
        ];
