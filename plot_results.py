#!/usr/bin/env python3

import matplotlib.pyplot as plt
import numpy as np
import csv
import sys
import os

def load_results(filename):
    """Load benchmark results from CSV file"""
    results = {}
    if not os.path.exists(filename):
        return results
    
    with open(filename, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            lang = row['language']
            impl = row['implementation']  # 'native' or 'cpp_wrapper'
            concurrency = int(row['concurrency'])
            p50 = float(row['p50'])
            p90 = float(row['p90'])
            p95 = float(row['p95'])
            
            if lang not in results:
                results[lang] = {}
            if impl not in results[lang]:
                results[lang][impl] = {'concurrency': [], 'p50': [], 'p90': [], 'p95': []}
            
            results[lang][impl]['concurrency'].append(concurrency)
            results[lang][impl]['p50'].append(p50)
            results[lang][impl]['p90'].append(p90)
            results[lang][impl]['p95'].append(p95)
    
    return results

def plot_comparison(results, language, output_dir='plots'):
    """Create comparison plots for a specific language"""
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    if language not in results:
        print(f"No results found for {language}")
        return
    
    lang_results = results[language]
    
    # Create subplots for P50, P90, P95
    fig, axes = plt.subplots(1, 3, figsize=(18, 6))
    fig.suptitle(f'{language} Cache Performance: Native vs C++ Wrapper', fontsize=16)
    
    metrics = ['p50', 'p90', 'p95']
    titles = ['P50 Latency', 'P90 Latency', 'P95 Latency']
    
    for i, (metric, title) in enumerate(zip(metrics, titles)):
        ax = axes[i]
        
        # Plot native implementation
        if 'native' in lang_results:
            native = lang_results['native']
            ax.plot(native['concurrency'], native[metric], 
                   marker='o', linewidth=2, markersize=8, 
                   label='Native', color='blue')
        
        # Plot C++ wrapper implementation
        if 'cpp_wrapper' in lang_results:
            cpp_wrapper = lang_results['cpp_wrapper']
            ax.plot(cpp_wrapper['concurrency'], cpp_wrapper[metric], 
                   marker='s', linewidth=2, markersize=8, 
                   label='C++ Wrapper', color='red')
        
        ax.set_xlabel('Concurrency Level')
        ax.set_ylabel('Latency (ns)')
        ax.set_title(title)
        ax.legend()
        ax.grid(True, alpha=0.3)
        ax.set_yscale('log')  # Log scale for better visualization
    
    plt.tight_layout()
    plt.savefig(f'{output_dir}/{language.lower()}_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()

def plot_combined_comparison(results, output_dir='plots'):
    """Create combined comparison plots for all languages"""
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    languages = list(results.keys())
    if not languages:
        print("No results to plot")
        return
    
    metrics = ['p50', 'p90', 'p95']
    titles = ['P50 Latency Comparison', 'P90 Latency Comparison', 'P95 Latency Comparison']
    
    fig, axes = plt.subplots(1, 3, figsize=(20, 6))
    fig.suptitle('Cross-Language Performance Comparison', fontsize=16)
    
    colors = {'Go': {'native': 'blue', 'cpp_wrapper': 'lightblue'}, 
              'Java': {'native': 'red', 'cpp_wrapper': 'lightcoral'}}
    
    for i, (metric, title) in enumerate(zip(metrics, titles)):
        ax = axes[i]
        
        for lang in languages:
            lang_results = results[lang]
            
            # Plot native implementation
            if 'native' in lang_results:
                native = lang_results['native']
                ax.plot(native['concurrency'], native[metric], 
                       marker='o', linewidth=2, markersize=6, 
                       label=f'{lang} Native', color=colors[lang]['native'])
            
            # Plot C++ wrapper implementation
            if 'cpp_wrapper' in lang_results:
                cpp_wrapper = lang_results['cpp_wrapper']
                ax.plot(cpp_wrapper['concurrency'], cpp_wrapper[metric], 
                       marker='s', linewidth=2, markersize=6, linestyle='--',
                       label=f'{lang} C++ Wrapper', color=colors[lang]['cpp_wrapper'])
        
        ax.set_xlabel('Concurrency Level')
        ax.set_ylabel('Latency (ns)')
        ax.set_title(title)
        ax.legend()
        ax.grid(True, alpha=0.3)
        ax.set_yscale('log')
    
    plt.tight_layout()
    plt.savefig(f'{output_dir}/combined_comparison.png', dpi=300, bbox_inches='tight')
    plt.close()

def main():
    # Load results
    results = load_results('benchmark_results.csv')
    
    if not results:
        print("No benchmark results found. Please run the benchmarks first.")
        print("Expected file: benchmark_results.csv")
        return
    
    # Create individual language plots
    for language in results.keys():
        plot_comparison(results, language)
        print(f"Created plot for {language}")
    
    # Create combined plot
    plot_combined_comparison(results)
    print("Created combined comparison plot")
    
    print("All plots saved to 'plots' directory")

if __name__ == "__main__":
    main() 