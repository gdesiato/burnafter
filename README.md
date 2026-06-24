# Experimental Platform for Fault Tolerance Evaluation

This repository contains the implementation developed for the MSc dissertation:

"An Empirical Evaluation of Fault Tolerance Strategies and Their Impact on Eventual Consistency in Transactional Microservices"

## Research Objective

The platform was developed to experimentally evaluate the impact of application-level fault tolerance strategies on performance, resilience, and eventual consistency within a transactional microservices environment.

## Architecture

- Message Service (2 instances)
- Audit Service
- PostgreSQL databases
- Transactional Outbox pattern
- Resilience4j retries
- Resilience4j circuit breakers
- Nginx load balancing
- Prometheus monitoring
- Grafana dashboards
- k6 workload generation

## Experimental Scenarios

- Baseline
- Retry
- Retry + Circuit Breaker
- Downstream Service Outage

## Running the Platform

docker compose up --build