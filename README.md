# ConceptEmbeddingModel

A concept embedding model for UMLS semantic relatedness measure.

## Overview

This repository contains the implementation of a concept embedding model designed to measure semantic relatedness in biomedical information ontologies, particularly focusing on the Unified Medical Language System (UMLS). The approach leverages external knowledge sources like Wikipedia to enhance concept vector representations, addressing limitations in traditional models.

The corresponding research paper, ["Concept embedding to measure semantic relatedness for biomedical information ontologies"](https://pubmed.ncbi.nlm.nih.gov/31009761/), provides detailed insights into the methodology and evaluation.

## Features

- Utilizes UMLS and external resources like Wikipedia for concept vectorization.
- Measures semantic relatedness using cosine similarity.
- Improved coverage and performance compared to traditional models.

## Repository Structure

- **dockerfiles/**: Contains Docker configurations for setting up the environment.
- **externalResources/**: External data sources and references.
- **files/**: Miscellaneous files used in the project.
- **lib/**: Core libraries and modules.
- **models/**: Pretrained models and related configurations.
- **sql/mysql/**: SQL scripts for database setup and management.
- **src/**: Source code for the embedding model.
- **target/**: Compiled code and build artifacts.

## Installation

1. Clone the repository:

```bash
git clone https://github.com/junseokpark/ConceptEmbeddingModel.git
cd ConceptEmbeddingModel
